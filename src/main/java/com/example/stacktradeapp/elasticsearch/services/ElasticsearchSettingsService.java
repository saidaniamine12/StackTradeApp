package com.example.stacktradeapp.elasticsearch.services;

import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface ElasticsearchSettingsService {
    //get the maximum number of documents that can be returned in a single search query
    int getMaxResultWindow() throws IOException;
    //set the maximum number of documents that can be returned in a single search query
    PutIndicesSettingsResponse setMaxResultWindow() throws IOException;
}
