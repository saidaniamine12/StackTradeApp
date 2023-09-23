package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JiraServerTicketRepository extends JpaRepository<JiraServerTicket, String> {
    @Query(value = "SELECT jst.* FROM jira_server_ticket jst Inner Join fields ON jst.fields_id = fields.id Order by updated DESC LIMIT :maxResults", nativeQuery = true)
    List<JiraServerTicket> findLatestResolvedTickets(Integer maxResults);


    @Query(value = "SELECT jst.* FROM jira_server_ticket jst WHERE id IN :ids", nativeQuery = true)
    List<JiraServerTicket> findJiraServerTicketsByIds(List<String> ids);

    @Query(value = "select jst.* from jira_server_ticket jst left join fields f ON jst.fields_id = f.id left join project ON project.id = f.project_id where project.key = :projectKey order by f.resolutiondate DESC LIMIT :maxResults", nativeQuery = true)
    List<JiraServerTicket> findJiraServerTicketsByProjectKey(String projectKey, int maxResults);
}
