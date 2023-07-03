package com.example.stacktradeapp.elasticsearch.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;

@Document(indexName = "spring_jira_index")
@Data
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



    public JiraTicket() {
    }


}
