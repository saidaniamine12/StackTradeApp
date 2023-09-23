package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/tickets")
@CrossOrigin(origins = "https://localhost:4200",allowedHeaders = "*")
public interface SearchController {

    @GetMapping("/search")
    ResponseEntity<List<JiraServerTicket>> semanticSearchOnField(@RequestParam(value = "query",defaultValue = "") String query,
                                                                @RequestParam(value = "selectedField", defaultValue = "All") String fieldName,
                                                                @RequestParam(value = "ticketsPerPage", defaultValue = "10") int ticketsPerPage
    );

    @GetMapping("/ticket/{id}")
    ResponseEntity<?> getTicketById(@PathVariable(value = "id") String id);

    @GetMapping("/latestViewedTickets")
    ResponseEntity<List<JiraServerTicket>> getLatestViewedTickets(
            @RequestParam(value = "ticketsPerPage", defaultValue = "10") int ticketsPerPage
    );

    @PostMapping("/latestViewedTicket/save")
    ResponseEntity<?> saveViewedTicket(@RequestBody String ticket_id);

    @GetMapping("/latest")
    ResponseEntity<List<JiraServerTicket>> getLatestResolvedTickets(
            @RequestParam(value = "ticketsPerPage", defaultValue = "10") int ticketsPerPage
    );

    @GetMapping("/project/{projectKey}")
    ResponseEntity<List<JiraServerTicket>> getTicketByProject(@PathVariable(value = "projectKey") String projectKey,
                                                              @RequestParam(value = "ticketsPerPage", defaultValue = "10" ) int ticketsPerPage);



}
