package com.example.stacktradeapp.models;

import com.google.type.DateTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Entity
@Builder
@Table(name = "viewed_tickets")
public class ViewedTicket {
    @Id
    @GeneratedValue
    private Long id;

    private String ticketId;

    @ManyToOne
    private User user;

    private ZonedDateTime viewedAt;

}
