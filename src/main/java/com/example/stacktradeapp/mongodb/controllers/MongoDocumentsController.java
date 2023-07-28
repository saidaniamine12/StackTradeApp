package com.example.stacktradeapp.mongodb.controllers;

import com.example.stacktradeapp.mongodb.services.MongoJiraTicketServiceImpl;
import com.mongodb.BasicDBObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
public class MongoDocumentsController {
    public static final Logger logger = LogManager.getLogger(MongoDocumentsController.class);


    private final MongoJiraTicketServiceImpl mongoJiraTicketService;




    public MongoDocumentsController(MongoJiraTicketServiceImpl mongoJiraTicketService) {
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


}
