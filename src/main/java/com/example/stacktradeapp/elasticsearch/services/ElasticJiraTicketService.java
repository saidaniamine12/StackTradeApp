package com.example.stacktradeapp.elasticsearch.services;

import com.example.stacktradeapp.elasticsearch.documents.JiraTicket;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ElasticJiraTicketService {
    //text search query method to search for tickets that match the search text
    List<JiraTicket> textSearchQuery(String searchText) throws IOException;

    //get the latest created tickets
    List<JiraTicket> getLatestCreatedTickets(Integer pageNumber,Integer ticketsPerPage) throws IOException;

    //get the total number of documents in the index
    Long getIndexSize() throws IOException;
}
