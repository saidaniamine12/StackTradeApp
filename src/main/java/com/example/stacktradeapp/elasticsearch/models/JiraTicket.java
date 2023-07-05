package com.example.stacktradeapp.elasticsearch.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;

@Document(indexName = "spring_jira_index")
@Data
@NoArgsConstructor
public class JiraTicket {

    @Id
    private String id;

    private String issueTypeName;
    private String projectName;
    private String resolutionName;
    private Date resolutionDate;
    private String assigneeEmailAddress;
    private String statusName;
    private String description;
    private String summary;
    private String creatorEmailAddress;
    private String reporterEmailAddress;
    private Date created;
    private Date updated;
    private String assigneeName;
    private String creatorName;
    private String reporterName;



    public JiraTicket(JiraTicket source) {
        this.id = source.id;
        this.issueTypeName = source.issueTypeName;
        this.projectName = source.projectName;
        this.resolutionName = source.resolutionName;
        this.resolutionDate = source.resolutionDate;
        this.assigneeEmailAddress = source.assigneeEmailAddress;
        this.statusName = source.statusName;
        this.description = source.description;
        this.summary = source.summary;
        this.creatorEmailAddress = source.creatorEmailAddress;
        this.reporterEmailAddress = source.reporterEmailAddress;
        this.created = source.created;
        this.updated = source.updated;
        this.assigneeName = source.assigneeName;
        this.creatorName = source.creatorName;
        this.reporterName = source.reporterName;
    }



}
