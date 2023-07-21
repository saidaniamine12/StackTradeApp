package com.example.stacktradeapp.milvus.services;


import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface MilvusSearchService {


    List<String> SemanticSearchOnSummaryField(String query, int topK);

    List<String> SemanticSearchOnDescriptionField(String query,int topK);

    List<String> combinedSemanticSearch(String query, int topK);
}
