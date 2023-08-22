package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.ViewedTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewedTicketsRepository extends JpaRepository<ViewedTicket, Long> {

    @Query(value = "SELECT * FROM  viewed_tickets vt  WHERE vt.user_id = :userId ORDER BY vt.viewed_at DESC LIMIT :ticketsPerPage ;", nativeQuery = true)
    List<ViewedTicket> getLatestViewedTickets(Integer userId, int ticketsPerPage );

    @Query(value = "SELECT * FROM  viewed_tickets vt  WHERE vt.ticket_id = :ticketId AND vt.user_id = :userId LIMIT 1  ;", nativeQuery = true)
    Optional<ViewedTicket> findByTicketAndUser(String ticketId, Integer userId);
}
