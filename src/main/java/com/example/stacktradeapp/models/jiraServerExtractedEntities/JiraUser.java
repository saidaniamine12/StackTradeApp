package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class JiraUser {

    @Id
    private String key;

    private String name;

    private String displayName;
    private boolean active;
    private String timeZone;
}
