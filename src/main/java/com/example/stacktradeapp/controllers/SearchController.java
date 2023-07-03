package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.entities.SearchEntity;
import com.example.stacktradeapp.entities.SearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RequestMapping("/tickets")
@CrossOrigin(origins = "http://localhost:4200",allowedHeaders = "*")
public interface SearchController {


    @GetMapping("/latest")
    ResponseEntity<SearchResponse> getLatestCreatedTickets(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int ticketsPerPage
    ) throws IOException;

    ResponseEntity<SearchResponse> searchTickets(@RequestParam(value = "query",defaultValue = "") String query,
                                                     @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
                                                     @RequestParam(value = "ticketsPerPage", defaultValue = "10") int ticketsPerPage
    ) throws IOException;

}
