package com.example.stacktradeapp.entities;

import com.example.stacktradeapp.elasticsearch.entities.JiraTicket;
import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.Date;

@Data
@EntityScan
public class SearchEntity {
    private String id;
    private String summary;
    private String projectName;
    private String description;

    private Date created;

    private String creatorName;
    private String creatorEmailAddress;



    //all args constructor
    public SearchEntity(String id,
                        String summary,
                        String projectName,
                        String description,
                        Date created,
                        String creatorName,
                        String creatorEmailAddress) {
        this.id = id;
        this.summary = summary;
        this.projectName = projectName;
        this.description = description;
        this.created = created;
        this.creatorName = creatorName;
        this.creatorEmailAddress = creatorEmailAddress;

    }

    //no args constructor
    public SearchEntity() {
    }

    public static SearchEntity fromJiraTicket(JiraTicket jiraTicket) {
        return new SearchEntity(
                jiraTicket.getId(),
                jiraTicket.getSummary(),
                jiraTicket.getProjectName(),
                jiraTicket.getDescription(),
                jiraTicket.getCreated(),
                jiraTicket.getCreatorName(),
                jiraTicket.getCreatorEmailAddress()
        );
    }




}
