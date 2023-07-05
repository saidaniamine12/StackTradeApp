package com.example.stacktradeapp.elasticsearch.services;

import com.example.stacktradeapp.elasticsearch.models.ElasticResponseEntity;
import com.example.stacktradeapp.elasticsearch.models.JiraTicket;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface ElasticJiraTicketService {
    //text search query method to search for tickets that match the search text
    //the search text is searched in the summary and description fields
    //ElasticsearchResponseEntity is a custom class that contains the list of tickets that match the search text
    //and the total number of tickets that match the search text
    ElasticResponseEntity textSearchQuery(String searchText, Integer pageNumber, Integer ticketsPerPage) ;

    //get the latest created tickets
    ElasticResponseEntity getLatestCreatedTickets(Integer pageNumber,Integer ticketsPerPage) ;

    //get the total number of documents in the index
    Long getIndexSize() throws IOException;

    //get ticket by id
    JiraTicket getTicketById(String id);
}
