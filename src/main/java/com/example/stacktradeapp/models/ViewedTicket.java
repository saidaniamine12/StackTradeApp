package com.example.stacktradeapp.models;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import com.google.type.DateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Entity
@AllArgsConstructor
@Table(name = "viewed_tickets")
public class ViewedTicket {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", nullable = false)
    private JiraServerTicket ticket;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private ZonedDateTime viewedAt;

    public ViewedTicket() {

    }
}
