package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.ViewedTicket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ViewedTicketsRepository extends JpaRepository<ViewedTicket, Long> {

    @Query(value = "SELECT  ticket_id FROM viewed_tickets WHERE user_id = :userId ORDER BY viewed_at DESC LIMIT :ticketsPerPage ", nativeQuery = true)
    Set<String> getLatestViewedTickets(Integer userId, int ticketsPerPage );
}
