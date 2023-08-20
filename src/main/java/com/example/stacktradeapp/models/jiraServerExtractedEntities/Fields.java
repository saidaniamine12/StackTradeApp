package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "fields")
public class Fields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "resolution_id", nullable = true)
    private Resolution resolution;

    @ManyToOne
    @JoinColumn(name = "assignee_id", nullable = true)
    private JiraUser assignee;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = true)
    private JiraUser reporter;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private JiraUser creator;

    @ManyToOne
    @JoinColumn(name = "issue_type_id", nullable = false)
    private IssueType issuetype;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private Instant resolutiondate;

    private Instant created;

    private Instant updated;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String summary;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "comment_id") // Adjust the column name as needed
    private Comment comment;

    @ManyToOne
    private Status status;

}
