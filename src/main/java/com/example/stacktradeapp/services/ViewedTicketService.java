package com.example.stacktradeapp.services;

import com.example.stacktradeapp.models.SimpleTicketDTO;
import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.models.ViewedTicket;
import com.example.stacktradeapp.repositories.ViewedTicketsRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ViewedTicketService {

    private final ViewedTicketsRepository viewedTicketRepository;

    public ViewedTicketService(ViewedTicketsRepository viewedTicketRepository) {
        this.viewedTicketRepository = viewedTicketRepository;
    }


    public void saveViewedTicket(String ticketId) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDateTime localDateTime = LocalDateTime.now();

        ZoneId currentZone = ZoneId.systemDefault(); // Get the current time zone
        ZonedDateTime zonedDateTime = localDateTime.atZone(currentZone);
        ViewedTicket viewedTicket = ViewedTicket.builder()
                .ticketId(ticketId)
                .user(userDetails)
                .viewedAt(zonedDateTime)
                .build();
        viewedTicketRepository.save(viewedTicket);
    }

    public List<String> getLatestViewedTicketsIds(int ticketsPerPage) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer userId = userDetails.getId();
        Set<String> ids = viewedTicketRepository.getLatestViewedTickets(userId, ticketsPerPage+1);
        return new ArrayList<>(ids);

    }
}
