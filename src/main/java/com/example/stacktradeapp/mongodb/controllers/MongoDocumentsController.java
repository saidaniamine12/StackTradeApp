package com.example.stacktradeapp.mongodb.controllers;

import com.example.stacktradeapp.mongodb.services.MongoJiraTicketService;
import com.mongodb.BasicDBObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController("/")
public class MongoDocumentsController {
    public static final Logger logger = LogManager.getLogger(MongoDocumentsController.class);


    private final MongoJiraTicketService mongoJiraTicketService;



    @Autowired
    public MongoDocumentsController(MongoJiraTicketService mongoJiraTicketService) {
        this.mongoJiraTicketService = mongoJiraTicketService;

    }


    @GetMapping("/tickets/{id}")
    public ResponseEntity<BasicDBObject> getTicketById(@PathVariable("id") String id){
        BasicDBObject document = mongoJiraTicketService.getTicketById(id);
        logger.info("Fetched document with id: " + id);
        if (document != null) {
            // Return 200 OK with the document as the response body
            return ResponseEntity.ok(document);
        } else {
            // Return 404 Not Found with a custom message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<BasicDBObject>> searchTickets(@RequestParam("query") String query) throws IOException {

        //check that the query is ot empty string
        if (!StringUtils.hasText(query)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<BasicDBObject> documentList = mongoJiraTicketService.searchTickets(query);
        if (!documentList.isEmpty()) {
            // Return 200 OK with the document as the response body
            return ResponseEntity.ok(documentList);
        } else {
            // Return 404 Not Found with a custom message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
