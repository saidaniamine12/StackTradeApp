package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Comments {
    @Id
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private JiraUser author;

    @Column(columnDefinition = "TEXT")
    private String body;

    private Instant updated;

    private Instant created;

}
