package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Resolution {
        @Id
        private String id;
        @Column(columnDefinition = "TEXT")
        private String description;
        private String name;

}
