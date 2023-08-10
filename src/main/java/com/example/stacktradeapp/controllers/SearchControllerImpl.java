package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.enums.FieldOption;
import com.example.stacktradeapp.models.SearchEntity;
import com.example.stacktradeapp.milvus.services.MilvusSearchService;
import com.example.stacktradeapp.mongodb.services.MongoJiraTicketServiceImpl;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchControllerImpl implements SearchController {

    private final Logger logger = LoggerFactory.getLogger(SearchControllerImpl.class);

    private final MilvusSearchService milvusSearchService;
    private final MongoJiraTicketServiceImpl mongoJiraTicketService;

    @Autowired
    public SearchControllerImpl(MilvusSearchService milvusSearchService, MongoJiraTicketServiceImpl mongoJiraTicketService) {
        this.milvusSearchService = milvusSearchService;
        this.mongoJiraTicketService = mongoJiraTicketService;
    }


    @Override
    public ResponseEntity<List<SearchEntity>> semanticSearchOnField(String query,String fieldName ,int ticketsPerPage) {

        logger.info("Semantic Search on field: " + fieldName + " with query: " + query);

        FieldOption fieldOption = FieldOption.valueOf(fieldName);

        try {

            switch (fieldOption) {
                case All -> {

                    List<String> topIds = milvusSearchService.combinedSemanticSearch(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(topIds);
                    List<SearchEntity> searchEntities = SearchEntity.basicDocToSearchEntity(topDocuments);
                    return ResponseEntity.ok(searchEntities);

                }
                case Summary -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnSummaryField(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(topIds);
                    List<SearchEntity> searchEntities = SearchEntity.basicDocToSearchEntity(topDocuments);
                    return ResponseEntity.ok(searchEntities);
                }
                case Description -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnDescriptionField(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(topIds);
                    List<SearchEntity> searchEntities = SearchEntity.basicDocToSearchEntity(topDocuments);
                    return ResponseEntity.ok(searchEntities);
                }

                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            }
        } catch (Exception e) {
            logger.error("error when processing the user's request: ",e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Override
    public ResponseEntity<?> getTicketById(String id) {

        try {
            BasicDBObject document = mongoJiraTicketService.getTicketById(id);

            if (document != null) {
                logger.info("Fetched document with id: " + id);
                // Return 200 OK with the document as the response body
                SearchEntity searchEntity = new SearchEntity(document);
                return ResponseEntity.ok(searchEntity);
            } else {
                // Return 404 Not Found with a custom message
                Map<String, String> map = new HashMap<>();
                map.put("message", "The requested resource was not found.");
                map.put("code", "NOT_FOUND");
                JSONObject jsonResponse = new JSONObject(map);
                logger.error("HTTP 400 - Not Found: The requested resource was not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResponse.toString());
            }
        } catch (Exception e) {
            logger.error("error when processing the user's request: ",e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }



}
