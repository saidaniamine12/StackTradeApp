package com.example.stacktradeapp.elasticsearch.controllers;

import com.example.stacktradeapp.elasticsearch.documents.JiraTicket;
import com.example.stacktradeapp.elasticsearch.services.ElasticJiraTicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/questions")
public class JiraTicketController {

    private final ElasticJiraTicketService elasticJiraTicketService;

    @Autowired
    public JiraTicketController(ElasticJiraTicketService elasticJiraTicketService) {
        this.elasticJiraTicketService = elasticJiraTicketService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<JiraTicket> getTicketById(@PathVariable("id") String id){
         JiraTicket jiraTicket = elasticJiraTicketService.getTicketById(id);
         if (jiraTicket != null){
             return new ResponseEntity<>(jiraTicket, HttpStatus.OK);
         }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
