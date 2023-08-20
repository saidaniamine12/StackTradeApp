package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Status {
    @Id
    private String id;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String name;
    @ManyToOne
    private StatusCategory statusCategory;

}

