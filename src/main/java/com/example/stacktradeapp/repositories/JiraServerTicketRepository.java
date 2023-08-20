package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraServerTicketRepository extends JpaRepository<JiraServerTicket, String> {
}
