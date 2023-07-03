package com.example.stacktradeapp.elasticsearch.repository;

import com.example.stacktradeapp.elasticsearch.entities.JiraTicket;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraTicketRepository extends ElasticsearchRepository<JiraTicket,String> {

    public JiraTicket findJiraTicketById(String Id);
}
