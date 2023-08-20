package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "jira_server_ticket")
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraServerTicket {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    private String id;

    @Column(nullable = false,unique = true,updatable = false)
    private String key;

    private String self;

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinColumn(name = "fields_id",referencedColumnName = "id")
    private Fields fields;
}
