package com.example.stacktradeapp.controllers.Impl;

import com.example.stacktradeapp.controllers.SearchController;
import com.example.stacktradeapp.elasticsearch.documents.JiraTicket;
import com.example.stacktradeapp.elasticsearch.services.ElasticJiraTicketService;
import com.example.stacktradeapp.entities.SearchEntity;
import com.example.stacktradeapp.entities.SearchResponse;
import com.example.stacktradeapp.mongodb.controllers.MongoDocumentsController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
public class SearchControllerImpl implements SearchController {
    public static final Logger logger = LogManager.getLogger(SearchControllerImpl.class);

    private final ElasticJiraTicketService elasticJiraTicketService;

    public SearchControllerImpl(ElasticJiraTicketService elasticJiraTicketService) {
        this.elasticJiraTicketService = elasticJiraTicketService;

    }


    @Override
    public ResponseEntity<SearchResponse> getLatestCreatedTickets(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int ticketsPerPage
    ) throws IOException {
        logger.info("page number: " + pageNumber + ", tickets per page: " + ticketsPerPage);

        List<JiraTicket> jiraTicketList = elasticJiraTicketService.getLatestCreatedTickets(pageNumber, ticketsPerPage);
        List<SearchEntity> searchEntityList = new ArrayList<>();
        //convert the list of jira tickets to a list of search entities
        for (JiraTicket jiraTicket : jiraTicketList) {
            //create a new search entity for each jira ticket
            SearchEntity searchEntity = new SearchEntity(
                    jiraTicket.getId(),
                    jiraTicket.getSummary(),
                    jiraTicket.getProjectName(),
                    jiraTicket.getDescription(),
                    jiraTicket.getCreated(),
                    jiraTicket.getCreatorName(),
                    jiraTicket.getCreatorEmailAddress()
            );
            searchEntityList.add(searchEntity);
        }
        Long totalNumberOfDocuments = elasticJiraTicketService.getIndexSize();
        logger.info("total number of documents: " + totalNumberOfDocuments);

        SearchResponse searchResponse = new SearchResponse(searchEntityList, totalNumberOfDocuments);

        if (!searchEntityList.isEmpty() && totalNumberOfDocuments != null) {
            // Return 200 OK with the document as the response body
            return ResponseEntity.ok(searchResponse);
        } else {
            // Return 404 Not Found with a custom message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }






    @Override

    @GetMapping("/search")
    public ResponseEntity<List<SearchEntity>> searchTickets(@RequestParam("query") String query) throws IOException {
        logger.info("Searching for query: " + query);

        //check that the query is ot empty string
        if (!StringUtils.hasText(query)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<JiraTicket> jiraTicketList = elasticJiraTicketService.textSearchQuery(query);
        List<SearchEntity> searchEntityList = new ArrayList<>();
        //convert the list of jira tickets to a list of search entities
        for (JiraTicket jiraTicket : jiraTicketList) {
            //create a new search entity for each jira ticket
            SearchEntity searchEntity = new SearchEntity(
                    jiraTicket.getId(),
                    jiraTicket.getSummary(),
                    jiraTicket.getProjectName(),
                    jiraTicket.getDescription(),
                    jiraTicket.getCreated(),
                    jiraTicket.getCreatorName(),
                    jiraTicket.getCreatorEmailAddress()

            );
            searchEntityList.add(searchEntity);
        }
        if (!searchEntityList.isEmpty()) {
            // Return 200 OK with the document as the response body
            return ResponseEntity.ok(searchEntityList);
        } else {
            // Return 404 Not Found with a custom message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
