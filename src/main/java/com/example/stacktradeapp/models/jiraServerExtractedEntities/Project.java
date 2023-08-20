package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Project {
    @Id
    private String id;

    private String key;

    private String name;

    private String projectTypeKey;

    @ManyToOne
    @JoinColumn(name = "project_category_id" , nullable = true)
    private ProjectCategory projectCategory;

}
