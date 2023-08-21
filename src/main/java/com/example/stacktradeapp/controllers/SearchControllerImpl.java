package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.enums.FieldOption;
import com.example.stacktradeapp.models.SimpleTicketDTO;
import com.example.stacktradeapp.milvus.services.MilvusSearchService;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import com.example.stacktradeapp.mongodb.services.MongoJiraTicketServiceImpl;
import com.example.stacktradeapp.services.JiraServerTicketService;
import com.example.stacktradeapp.services.ViewedTicketService;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchControllerImpl implements SearchController {

    private final Logger logger = LoggerFactory.getLogger(SearchControllerImpl.class);

    private final MilvusSearchService milvusSearchService;
    private final MongoJiraTicketServiceImpl mongoJiraTicketService;

    private final JiraServerTicketService jiraServerTicketService;

    private final ViewedTicketService viewedTicketService;

    @Autowired
    public SearchControllerImpl(MilvusSearchService milvusSearchService, MongoJiraTicketServiceImpl mongoJiraTicketService, JiraServerTicketService jiraServerTicketService, ViewedTicketService viewedTicketService) {
        this.milvusSearchService = milvusSearchService;
        this.mongoJiraTicketService = mongoJiraTicketService;
        this.jiraServerTicketService = jiraServerTicketService;
        this.viewedTicketService = viewedTicketService;
    }


    @Override
    public ResponseEntity<List<SimpleTicketDTO>> semanticSearchOnField(String query, String fieldName , int ticketsPerPage) {

        logger.info("Semantic Search on field: " + fieldName + " with query: " + query);

        FieldOption fieldOption = FieldOption.valueOf(fieldName);

        try {
            switch (fieldOption) {
                case All -> {

                    List<String> topIds = milvusSearchService.combinedSemanticSearch(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(topIds);
                    List<SimpleTicketDTO> searchEntities = SimpleTicketDTO.basicDocToTicketDTOMapper(topDocuments);
                    return ResponseEntity.ok(searchEntities);

                }
                case Summary -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnSummaryField(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(topIds);
                    List<SimpleTicketDTO> searchEntities = SimpleTicketDTO.basicDocToTicketDTOMapper(topDocuments);
                    return ResponseEntity.ok(searchEntities);
                }
                case Description -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnDescriptionField(query, ticketsPerPage);
                    List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(topIds);
                    List<SimpleTicketDTO> searchEntities = SimpleTicketDTO.basicDocToTicketDTOMapper(topDocuments);
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
                SimpleTicketDTO simpleTicketDTO = new SimpleTicketDTO(document);
                return ResponseEntity.ok(simpleTicketDTO);
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

    @Override
    public ResponseEntity<List<SimpleTicketDTO>> getLatestViewedTickets(int ticketsPerPage) {
        try{
            List<String> ids =  viewedTicketService.getLatestViewedTicketsIds(ticketsPerPage);
            List<BasicDBObject> topDocuments = mongoJiraTicketService.getSortedTicketsByIds(ids);
            List<SimpleTicketDTO> searchEntities = SimpleTicketDTO.basicDocToTicketDTOMapper(topDocuments);
            return ResponseEntity.ok(searchEntities);
        }catch (Exception e){
            logger.error("error when processing the user's request: ",e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Override
    public ResponseEntity<?> saveViewedTicket(@RequestBody String ticket_id) {
        viewedTicketService.saveViewedTicket(ticket_id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<JiraServerTicket>> getLatestResolvedTickets(int ticketsPerPage){
        List<JiraServerTicket> returnedTickets = jiraServerTicketService.getLatestResolvedTickets(ticketsPerPage);
        return ResponseEntity.ok(returnedTickets);
    }


}
