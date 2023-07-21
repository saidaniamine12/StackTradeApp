package com.example.stacktradeapp.milvus.vectorRepository;

import io.milvus.client.MilvusClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.response.GetCollStatResponseWrapper;
import com.example.stacktradeapp.entities.VectorCollectionEntity;
import io.milvus.response.SearchResultsWrapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class MilvusRepository {
    private final Logger logger = Logger.getLogger(MilvusRepository.class.getName());

       private final MilvusClient milvusClient;


    public MilvusRepository(MilvusClient milvusClient) {

        this.milvusClient = milvusClient;
    }

    public List<SearchResultsWrapper.IDScore> search(String collectionName,
                                              String vectorFieldName,
                                              List<Float> vector,
                                              int topK) {
        System.out.println("========== queryVector() ==========");
        loadCollectionToMemory(collectionName);
        List<List<Float>> search_vectors = new ArrayList<>();
        search_vectors.add(vector);
        final Integer SEARCH_K = topK;                       // TopK
        final String SEARCH_PARAM = "{\"nprobe\":10, \"offset\":0}";    // Params

        List<String> search_output_fields = List.of("ticket_id");
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(MetricType.IP)
                .withOutFields(search_output_fields)
                .withTopK(SEARCH_K)
                .withVectors(search_vectors)
                .withVectorFieldName(vectorFieldName)
                .withParams(SEARCH_PARAM)
                .build();
        R<SearchResults> respSearch = milvusClient.search(searchParam);
        handleResponseStatus(respSearch);
        SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
        logger.info("Search results on"+ collectionName+ wrapperSearch.getIDScore(0));
        return wrapperSearch.getIDScore(0);
    }


    public Boolean hasCollection(String collectionName) {
        R<Boolean> respHasCollection = this.milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        if (respHasCollection.getData() == Boolean.TRUE) {
            System.out.println("Collection exists. Skip collection creation.");
            return true;
        } else {
            System.out.println("Collection  does not exists. Create collection. ");
            return false;
        }
    }

    public long getCollectionDescription (String collectionName){
        // call flush() to flush the insert buffer to storage,
        //// so that we can get correct number
        // call flush() to flush the insert buffer to storage,
        // so that the getCollectionStatistics() can get correct number
        flush(collectionName);
        System.out.println("========== respShowCollections ==========");
        R<ShowCollectionsResponse> respShowCollections = milvusClient.showCollections(
                ShowCollectionsParam.newBuilder().build()
        );
        System.out.println(respShowCollections);
        System.out.println("========== respShowCollections done ==========");


        System.out.println("========== respDescribeCollection ==========");
        R<DescribeCollectionResponse> respDescribeCollection = milvusClient.describeCollection(
                  // Return the name and schema of the collection.
                DescribeCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        System.out.println(respDescribeCollection);
        System.out.println("========== respDescribeCollection done ==========");


        System.out.println("========== respCollectionStatistics ==========");
        R<GetCollectionStatisticsResponse> respCollectionStatistics = milvusClient.getCollectionStatistics(
                // Return the statistics information of the collection.
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

        GetCollStatResponseWrapper wrapperCollectionStatistics = new GetCollStatResponseWrapper(respCollectionStatistics.getData());
        System.out.println("Collection row count: " + wrapperCollectionStatistics.getRowCount());
        return wrapperCollectionStatistics.getRowCount();

    }

    public void insertDocument(String collectionName,
                                      String collectionIdFieldName,
                                      String vectorFieldName,
                                      VectorCollectionEntity vectorCollectionEntity
                                      ) {


        List<Long> ticket_id_array = new ArrayList<>();
        ticket_id_array.add(vectorCollectionEntity.getId());
        List<List<Float>> summary_vector_array = new ArrayList<>();
        summary_vector_array.add(vectorCollectionEntity.getVector(true));


        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(collectionIdFieldName, ticket_id_array));
        fields.add(new InsertParam.Field(vectorFieldName, summary_vector_array));
        // Insert vectors to the collection.
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();
        R<MutationResult> result =  this.milvusClient.insert(insertParam);
        handleResponseStatus(result);
    }



    private void handleResponseStatus(R<?> r) {
        if (r.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(r.getMessage());
        }
    }

    //index a document
    public void indexVector(String collectionName,
                                   String vectorFieldName) {

        final IndexType INDEX_TYPE = IndexType.IVF_FLAT;   // IndexType
        final String INDEX_PARAM = "{\"nlist\":1024}";     // ExtraParam

        R<RpcStatus> result = this.milvusClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withFieldName(vectorFieldName)
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.IP)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build()
        );
        handleResponseStatus(result);
    }

    //drop an index
    public void dropIndex(String collectionName) {
        this.milvusClient.dropIndex(
                DropIndexParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
    }

    //create a collection
    public void createCollection(String collectionName,
                                        String vectorFieldName,
                                        int dimension,
                                        String description
    ) {
        if (hasCollection(collectionName)) {
            System.out.println("Collection exists.");
        } else {
            FieldType idField = FieldType.newBuilder()
                    .withName("ticket_id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .build();
            FieldType vectorField = FieldType.newBuilder()
                    .withName(vectorFieldName)
                    .withDataType(DataType.FloatVector)
                    .withDimension(dimension)
                    .build();
            CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription(description)
                    .withShardsNum(2)
                    .addFieldType(idField)
                    .addFieldType(vectorField)
                    .build();
            R<RpcStatus> response = this.milvusClient.createCollection(createCollectionReq);
            handleResponseStatus(response);
            System.out.println("Collection created.");
        }
    }

    //query a vector
    public void queryVector(
                            String collectionName,
                            String vectorFieldName,
                            List<List<Float>> vectors,
                            int topK) {
        this.milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

    }

    public void flush(String collectionName) {
        this.milvusClient.flush(
                FlushParam.newBuilder()
                        .addCollectionName(collectionName)
                        .build()
        );
    }

    public void loadCollectionToMemory(String collectionName){
                milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
    }
}
