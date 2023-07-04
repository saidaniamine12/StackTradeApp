package com.example.stacktradeapp.elasticsearch.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ElasticResponseEntity {
    List<JiraTicket> jiraTickets;
    Long totalHits;


    public List<JiraTicket> getJiraTicketList() {
        return jiraTickets;
    }
}
