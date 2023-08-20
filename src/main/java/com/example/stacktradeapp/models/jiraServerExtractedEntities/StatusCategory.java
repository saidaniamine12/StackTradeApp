package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class StatusCategory {
    @Id
    private Integer id;

    private String key;

    private String colorName;

    private String name;
}
