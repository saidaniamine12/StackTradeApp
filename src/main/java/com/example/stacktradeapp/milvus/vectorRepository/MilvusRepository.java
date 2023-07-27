package com.example.stacktradeapp.milvus.vectorRepository;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.response.GetCollStatResponseWrapper;
import com.example.stacktradeapp.entities.MilvusEntity;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Repository
public class MilvusRepository {

    private final Logger logger = LoggerFactory.getLogger(MilvusRepository.class);

    @Value("${com.example.stacktradeapp.milvus.summary.description.id.name}")
    private String ticketIdName;
    //private final Logger logger = Logger.getLogger(MilvusRepository.class.getName());

    private final MilvusClient milvusClient;


    public MilvusRepository(MilvusClient milvusClient) {

        this.milvusClient = milvusClient;
    }

    //search for top vectors in a collection
    //return a list of id and score
    public List<SearchResultsWrapper.IDScore> search(String collectionName,
                                              String vectorFieldName,
                                              List<Float> vector,
                                              int topK) {
        boolean isLoaded = loadCollectionToMemory(collectionName);
        if (!isLoaded) {
            logger.error("Collection is not loaded to memory");
            return null;
        }
        List<List<Float>> search_vectors = new ArrayList<>();           //creating a list of vectors to search
        search_vectors.add(vector);         //adding the vector to search
        final Integer SEARCH_K = topK;          // TopK neighbours
        final String SEARCH_PARAM = "{\"nprobe\":10, \"offset\":0}";    // search Params

        List<String> search_output_fields = List.of(ticketIdName);     // output fields
        // search param
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
        long time1 = System.currentTimeMillis();
        R<SearchResults> respSearch = milvusClient.search(searchParam);    // search
        long time2 = System.currentTimeMillis();
        logger.info("search time: " + (time2 - time1));
        handleResponseStatus(respSearch);      // handle response status
        SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());    // wrap search results
        logger.info("Search results on"+ collectionName+ wrapperSearch.getIDScore(0));    // log search results
        return wrapperSearch.getIDScore(0);    // return search results
    }


    //check if a collection exists
    public Boolean hasCollection(String collectionName) {
        R<Boolean> respHasCollection = this.milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        if (respHasCollection.getData() == Boolean.TRUE) {
            logger.error("Collection exists. Skip collection creation.");
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


    public static boolean insertDocuments(MilvusServiceClient milvusClient,
                                          String collectionName,
                                          List<MilvusEntity> milvusEntities
    ) {

        List<InsertParam.Field> fields = new ArrayList<>();
        String vectorFieldName = "";
        String idFieldName = "";

        R<DescribeCollectionResponse> respDescribeCollection = milvusClient.describeCollection(
                // Return the name and schema of the collection.
                DescribeCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        handleResponseStatus(respDescribeCollection);

        List<FieldSchema> fieldSchemaList = respDescribeCollection.getData().getSchema().getFieldsList();
        for (FieldSchema fieldSchema : fieldSchemaList) {
            if(fieldSchema.getIsPrimaryKey()) idFieldName = fieldSchema.getName();
            if(fieldSchema.getDataType().equals(DataType.FloatVector)) vectorFieldName = fieldSchema.getName();
        }

        for (MilvusEntity milvusEntity : milvusEntities) {
            List<Long> ticket_id_array = new ArrayList<>();
            ticket_id_array.add(milvusEntity.getId());
            List<List<Float>> vector_array = new ArrayList<>();
            vector_array.add(milvusEntity.getVector(true));
            fields.add(new InsertParam.Field(idFieldName, ticket_id_array));
            fields.add(new InsertParam.Field(vectorFieldName, vector_array));
            // Insert vectors to the collection.
        }

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();
        R<MutationResult> result =  milvusClient.insert(insertParam);
        milvusClient.flush(FlushParam.newBuilder().addCollectionName(collectionName).build());
        handleResponseStatus(result);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(result.getMessage());
        } else {
            System.out.println("Insert vectors to the collection done.");
            return true;
        }
    }


    //handle response status from Milvus server
    private static void handleResponseStatus(R<?> r) {
        if (r.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(r.getMessage());
        }
    }

    //building an index on a collection vector field
    public void indexVector(String collectionName,
                                   String vectorFieldName) {

        // Index parameters
        //Type of index used to accelerate the vector search (IVF_FlAT)
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
            logger.info("skipping creation of "+collectionName +", reason: Collection exists.");
        } else {
            FieldType idField = FieldType.newBuilder()
                    .withName(ticketIdName)
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

    //query a a Milvus entity from a collection by id
    public MilvusEntity queryMilvusEntity(
                            String collectionName,
                            String idFieldName,
                            String vectorFieldNAme,
                            String id) throws RuntimeException {

        loadCollectionToMemory(collectionName) ;

        List<String> query_output_fields = Arrays.asList(idFieldName,vectorFieldNAme);

        QueryParam queryParam = QueryParam.newBuilder()
                .withCollectionName(collectionName)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withExpr("ticket_id == "+id)
                .withOutFields(query_output_fields)
                .withOffset(0L)
                .withLimit(10L)
                .build();
        R<QueryResults> respQuery = milvusClient.query(queryParam);
        handleResponseStatus(respQuery);

        QueryResultsWrapper wrapperQuery = new QueryResultsWrapper(respQuery.getData());
        try {
            final Long idValue = (Long) wrapperQuery.getFieldWrapper(idFieldName).getFieldData().get(0);
            final ArrayList<?> vectorValue = new ArrayList<Object>(wrapperQuery.getFieldWrapper("summary_vector").getFieldData()) ;
            List<Float> floats = (List<Float>) vectorValue.get(0);
            if (idValue != null){
                if (floats.size() == 384){
                    float[] floatArray = new float[floats.size()];
                    int i =0;
                    for(Float f: floats){
                        floatArray[i++] =  f;
                    }
                    return new MilvusEntity(idValue,floatArray);
                }
            }
        } catch (Exception e){
            logger.error("Error fetching entity with Id " + id + " from milvus Collection with name "+collectionName);
        }



        System.out.println(wrapperQuery.getFieldWrapper(idFieldName).getFieldData());


        return null;
    }

    public void flush(String collectionName) {
        this.milvusClient.flush(
                FlushParam.newBuilder()
                        .addCollectionName(collectionName)
                        .build()
        );
    }

    public boolean loadCollectionToMemory(String collectionName){
        GetLoadingProgressParam.Builder getLoadingProgressParamBuilder = GetLoadingProgressParam.newBuilder().withCollectionName(collectionName);
        GetLoadingProgressParam getLoadingProgressParam = new GetLoadingProgressParam(getLoadingProgressParamBuilder);
        R<GetLoadingProgressResponse> r = milvusClient.getLoadingProgress(getLoadingProgressParam);
        if (r.getStatus() != R.Status.Success.getCode()) {
            logger.info("load "+ collectionName +" collection to memory....");
            R<RpcStatus> loadingResponse =  milvusClient.loadCollection(
                        LoadCollectionParam.newBuilder()
                                .withCollectionName(collectionName)
                                .build()
            );
            if (loadingResponse.getStatus() != R.Status.Success.getCode()) {
                logger.error("load collection failed: {}" ,loadingResponse.getMessage());
                return false;
            }
        }
        return true;
    }


}
