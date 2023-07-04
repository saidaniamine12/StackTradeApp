package com.example.stacktradeapp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/settings")
@RestController
public interface ElasticSettingsController {

    @GetMapping("/maxResultWindow")
    ResponseEntity<String> getElasticsearchSettings() throws IOException;

    //set the maximum number of documents that can be returned in a search query
    @PutMapping("/maxResultWindow")
    ResponseEntity<String> setElasticsearchSettings() throws IOException;

}
