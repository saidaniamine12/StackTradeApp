package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.entities.SearchEntity;
import com.mongodb.BasicDBObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/tickets")
@CrossOrigin(origins = "http://localhost:4200",allowedHeaders = "*")
public interface SearchController {


    @GetMapping("/search")
    ResponseEntity<List<SearchEntity>> semanticSearchOnField(@RequestParam(value = "query",defaultValue = "") String query,
                                                     @RequestParam(value = "selectedField", defaultValue = "all") String fieldName,
                                                     @RequestParam(value = "ticketsPerPage", defaultValue = "10") int ticketsPerPage
    );

}
