package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class IssueType {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String name;
    private Boolean subtask;


}
