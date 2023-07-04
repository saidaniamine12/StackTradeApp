package com.example.stacktradeapp.elasticsearch.services.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetIndicesSettingsResponse;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsResponse;
import com.example.stacktradeapp.elasticsearch.services.ElasticJiraTicketService;
import com.example.stacktradeapp.elasticsearch.services.ElasticsearchSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class ElasticsearchSettingsServiceImpl implements ElasticsearchSettingsService {

    private final Logger logger = LoggerFactory.getLogger(ElasticsearchSettingsServiceImpl.class);


    //get the indexname from the application.properties file
    @Value("${com.example.stacktradeapp.elasticsearch.indexName}")
    private String indexName;

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticJiraTicketService elasticJiraTicketService;



    @Autowired
    public ElasticsearchSettingsServiceImpl(ElasticsearchClient elasticsearchClient, ElasticJiraTicketService elasticJiraTicketService) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticJiraTicketService = elasticJiraTicketService;
    }

    @Override
    public int getMaxResultWindow() throws IOException {
         GetIndicesSettingsResponse getIndicesSettingsResponse = elasticsearchClient.indices().getSettings(
                 s -> s.index(indexName)
         );
         Integer maxResultWindow = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(getIndicesSettingsResponse.get(indexName)).settings()).index()).maxResultWindow();
         logger.info("maxResultWindow " + maxResultWindow);
         if (maxResultWindow != null) return maxResultWindow;
        else throw new IOException("maxResultWindow is null");
    }

    @Override
    public PutIndicesSettingsResponse setMaxResultWindow() throws IOException{
        int indexSize = Math.toIntExact(elasticJiraTicketService.getIndexSize());
        PutIndicesSettingsResponse indicesSettingsResponse =  elasticsearchClient.indices().putSettings(
                s -> s.index(indexName).settings(
                        i -> i.maxResultWindow(indexSize)
                ));
        int maxResultWindow = getMaxResultWindow();
        logger.info(indicesSettingsResponse.toString());
        logger.info("maxResultWindow " + maxResultWindow);
        return indicesSettingsResponse;
    }
}
