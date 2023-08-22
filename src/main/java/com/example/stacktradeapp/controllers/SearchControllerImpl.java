package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.enums.FieldOption;
import com.example.stacktradeapp.milvus.services.MilvusSearchService;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import com.example.stacktradeapp.services.TicketService;
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

    private final TicketService ticketService;

    @Autowired
    public SearchControllerImpl(MilvusSearchService milvusSearchService,
                                TicketService ticketService) {
        this.milvusSearchService = milvusSearchService;
        this.ticketService = ticketService;
    }


    @Override
    public ResponseEntity<List<JiraServerTicket>> semanticSearchOnField(String query, String fieldName , int ticketsPerPage) {

        logger.info("Semantic Search on field: " + fieldName + " with query: " + query);

        FieldOption fieldOption = FieldOption.valueOf(fieldName);

        try {
            switch (fieldOption) {
                case All -> {

                    List<String> topIds = milvusSearchService.combinedSemanticSearch(query, ticketsPerPage);
                    List<JiraServerTicket> topTickets = ticketService.getTicketsByIds(topIds);
                    return ResponseEntity.ok(topTickets);

                }
                case Summary -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnSummaryField(query, ticketsPerPage);
                    List<JiraServerTicket> topTickets = ticketService.getTicketsByIds(topIds);
                    return ResponseEntity.ok(topTickets);
                }
                case Description -> {

                    List<String> topIds = milvusSearchService.SemanticSearchOnDescriptionField(query, ticketsPerPage);
                    List<JiraServerTicket> topTickets = ticketService.getTicketsByIds(topIds);
                    return ResponseEntity.ok(topTickets);
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
            JiraServerTicket ticket = ticketService.getTicketById(id);

            if (ticket != null) {
                logger.info("Fetched ticket with id: " + id);
                // Return 200 OK with the document as the response body
                return ResponseEntity.ok(ticket);
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
    public ResponseEntity<List<JiraServerTicket>> getLatestViewedTickets(int ticketsPerPage) {
        logger.info("get latest viewed tickets");
        try{
            List<JiraServerTicket> tickets =  ticketService.getLatestViewedTickets(ticketsPerPage);
            return ResponseEntity.ok(tickets);
        }catch (Exception e){
            logger.error("error when processing the user's request: ",e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Override
    public ResponseEntity<?> saveViewedTicket(@RequestBody String ticket_id) {
        try{
            ticketService.saveViewedTicket(ticket_id);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            logger.error("error when processing the user's request: ",e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }

    @Override
    public ResponseEntity<List<JiraServerTicket>> getLatestResolvedTickets(int ticketsPerPage){
        List<JiraServerTicket> returnedTickets = ticketService.getLatestResolvedTickets(ticketsPerPage);
        return ResponseEntity.ok(returnedTickets);
    }


}
