package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "comments")
public class Comments {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = true)
    private JiraUser author;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = true)
    private Comment comment;

    private Instant updated;

    private Instant created;

}
