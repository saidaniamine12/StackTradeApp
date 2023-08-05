package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.models.SearchEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/tickets")
@CrossOrigin(origins = "https://localhost:4200",allowedHeaders = "*")
public interface SearchController {


    @GetMapping("/search")
    ResponseEntity<List<SearchEntity>> semanticSearchOnField(@RequestParam(value = "query",defaultValue = "") String query,
                                                     @RequestParam(value = "selectedField", defaultValue = "All") String fieldName,
                                                     @RequestParam(value = "ticketsPerPage", defaultValue = "10") int ticketsPerPage
    );

    @GetMapping("/ticket/{id}")
    ResponseEntity<?> getTicketById(@PathVariable(value = "id") String id);

}
