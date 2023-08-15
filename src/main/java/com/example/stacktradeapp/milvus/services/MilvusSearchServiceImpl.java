package com.example.stacktradeapp.milvus.services;

import com.example.stacktradeapp.milvus.vectorRepository.MilvusRepository;
import com.example.stacktradeapp.sentenceTransformers.SentenceTransformerService;
import io.milvus.response.SearchResultsWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MilvusSearchServiceImpl implements MilvusSearchService {

    private final Logger logger = LoggerFactory.getLogger(MilvusSearchServiceImpl.class);

    @Value("${com.example.stacktradeapp.milvus.summary.collection.name}")
    private String summaryCollectionName;

    @Value("${com.example.stacktradeapp.milvus.collections.id.field.name}")
    private String summaryCollectionIdFieldName;

    @Value("${com.example.stacktradeapp.milvus.summary.collection.vector.field.name}")
    private String summaryCollectionVectorFieldName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.name}")
    private String descriptionCollectionName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.id.field.name}")
    private String descriptionIdFieldName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.vector.field.name}")
    private String descriptionCollectionVectorFieldName;

    private final MilvusRepository milvusRepository;

    private final SentenceTransformerService sentenceTransformerService;






    @Autowired
    public MilvusSearchServiceImpl(MilvusRepository milvusRepository, SentenceTransformerService sentenceTransformerService) {
        this.milvusRepository = milvusRepository;
        this.sentenceTransformerService = sentenceTransformerService;
    }



    @Override
    public List<String> SemanticSearchOnSummaryField(String query, int topK){
        List<Float> queryVector = sentenceTransformerService.generateSymmetricEmbedding(query);
        return initiateSearch(summaryCollectionName, summaryCollectionIdFieldName, summaryCollectionVectorFieldName,queryVector, topK);
    }

    @NotNull
    private List<String> initiateSearch(String summaryCollectionName,String idFieldName, String summaryCollectionVectorFieldName, List<Float> queryVector ,int topK ) {
        List<SearchResultsWrapper.IDScore> idScoreList = milvusRepository.search(summaryCollectionName, idFieldName,summaryCollectionVectorFieldName,queryVector, topK);
        List<String> idList = new ArrayList<>();
        for (SearchResultsWrapper.IDScore idScore : idScoreList) {
            idList.add(Long.toString(idScore.getLongID()));
        }
        if (idScoreList.isEmpty() ) {
            logger.warn("idScoreList is empty");
            throw new IllegalArgumentException();
        }
        return idList;
    }

    @Override
    public List<String> SemanticSearchOnDescriptionField(String query, int topK){
        List<Float> queryVector = sentenceTransformerService.generateAsymmetricEmbedding(query);
        return initiateSearch(descriptionCollectionName,descriptionIdFieldName, descriptionCollectionVectorFieldName, queryVector, topK);
    }

    @Override
    public List<String> combinedSemanticSearch(String query, int topK) {

        List<Float> symmetricQueryVector = sentenceTransformerService.generateSymmetricEmbedding(query);
        List<Float> asymmetricQueryVector = sentenceTransformerService.generateAsymmetricEmbedding(query);
        if (symmetricQueryVector == null ) {
            logger.warn("symmetricQueryVector query vector is null");
            throw new IllegalArgumentException();
        }
        if (asymmetricQueryVector == null ) {
            logger.warn("asymmetricQueryVector query vector is null");
            throw new IllegalArgumentException();
        }

        topK = topK/2;
        List<SearchResultsWrapper.IDScore> symmetricIdScoreList = milvusRepository.search(summaryCollectionName, summaryCollectionIdFieldName,summaryCollectionVectorFieldName, symmetricQueryVector, topK);
        List<SearchResultsWrapper.IDScore> asymmetricIdScoreList = milvusRepository.search(descriptionCollectionName, descriptionIdFieldName,descriptionCollectionVectorFieldName, asymmetricQueryVector, topK);

        if (symmetricIdScoreList == null ) {
            logger.warn("symmetricIdScoreList is null");
            throw new IllegalArgumentException();
        }
        if (asymmetricIdScoreList == null ) {
            logger.warn("asymmetricIdScoreList is null");
            throw new IllegalArgumentException();
        }
        Map<String,Float> idsScoreMap = new HashMap<>();

        for (SearchResultsWrapper.IDScore idScore : symmetricIdScoreList) {
            idsScoreMap.put(Long.toString(idScore.getLongID()) ,idScore.getScore());
        }
        for (SearchResultsWrapper.IDScore idScore : asymmetricIdScoreList) {
            if(idsScoreMap.containsKey(Long.toString(idScore.getLongID()))){
                if(idsScoreMap.get(Long.toString(idScore.getLongID()))>idScore.getScore()){
                    continue;
                }
            }
            idsScoreMap.put(Long.toString(idScore.getLongID()),idScore.getScore());
        }


        List<String> idList = new ArrayList<>();
        List<Float> scoreList = new ArrayList<>();

        //sort the map by value and return a new list of keys only in descending order
        for (Map.Entry<String, Float> entry : idsScoreMap.entrySet()) {
            idList.add(entry.getKey());
            scoreList.add(entry.getValue());
        }

        for (int i = 0; i < idList.size(); i++) {
            for (int j = i+1; j < idList.size(); j++) {

                if(scoreList.get(i)<scoreList.get(j)){

                    String tempId = idList.get(i);
                    idList.set(i,idList.get(j));
                    idList.set(j,tempId);
                    Float tempScore = scoreList.get(i);
                    scoreList.set(i,scoreList.get(j));
                    scoreList.set(j,tempScore);
                }
            }
        }

        return idList;
    }


}
