package com.example.stacktradeapp.controllers.impl;

import com.example.stacktradeapp.controllers.SearchController;
import com.example.stacktradeapp.entities.SearchEntity;
import com.example.stacktradeapp.milvus.services.MilvusSearchService;
import com.example.stacktradeapp.mongodb.services.MongoJiraTicketService;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200",allowedHeaders = "*")
public class SearchControllerImpl implements SearchController {

    private final MilvusSearchService milvusSearchService;
    private final MongoJiraTicketService mongoJiraTicketService;

    @Autowired
    public SearchControllerImpl(MilvusSearchService milvusSearchService, MongoJiraTicketService mongoJiraTicketService) {
        this.milvusSearchService = milvusSearchService;
        this.mongoJiraTicketService = mongoJiraTicketService;
    }


    @Override
    public ResponseEntity<List<SearchEntity>> semanticSearchOnField(String query,String fieldName ,int ticketsPerPage) {

        try {

            switch (fieldName) {
                case "All" -> {

                    List<String> topIds = milvusSearchService.combinedSemanticSearch(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getTicketsByIds(topIds);
                    List<SearchEntity> searchEntities = SearchEntity.basicDocToSearchEntity(topDocuments);
                    return ResponseEntity.ok(searchEntities);

                }
                case "Summary" -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnSummaryField(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getTicketsByIds(topIds);
                    List<SearchEntity> searchEntities = SearchEntity.basicDocToSearchEntity(topDocuments);
                    return ResponseEntity.ok(searchEntities);

                }
                case "Description" -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnDescriptionField(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getTicketsByIds(topIds);
                    List<SearchEntity> searchEntities = SearchEntity.basicDocToSearchEntity(topDocuments);
                    return ResponseEntity.ok(searchEntities);

                }
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



}
