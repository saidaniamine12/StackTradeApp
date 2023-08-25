package com.example.stacktradeapp.services;

import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.models.ViewedTicket;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.*;
import com.example.stacktradeapp.repositories.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final JiraServerTicketRepository jiraServerTicketRepository;
    private final ResolutionRepository resolutionRepository;
    private final StatusCategoryRepository statusCategoryRepository;
    private final StatusRepository statusRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final JiraUserRepository jiraUserRepository;

    private final ViewedTicketsRepository viewedTicketsRepository;
    private final CommentsRepository commentsRepository;

    @Autowired
    public TicketService(JiraServerTicketRepository jiraServerTicketRepository,
                         ResolutionRepository resolutionRepository,
                         StatusCategoryRepository statusCategoryRepository,
                         StatusRepository statusRepository,
                         ProjectRepository projectRepository,
                         ProjectCategoryRepository projectCategoryRepository,
                         IssueTypeRepository issueTypeRepository,
                         JiraUserRepository jiraUserRepository, ViewedTicketsRepository viewedTicketsRepository, CommentsRepository commentsRepository) {
        this.jiraServerTicketRepository = jiraServerTicketRepository;
        this.resolutionRepository = resolutionRepository;
        this.statusCategoryRepository = statusCategoryRepository;
        this.statusRepository = statusRepository;
        this.projectRepository = projectRepository;
        this.projectCategoryRepository = projectCategoryRepository;
        this.issueTypeRepository = issueTypeRepository;
        this.jiraUserRepository = jiraUserRepository;
        this.viewedTicketsRepository = viewedTicketsRepository;
        this.commentsRepository = commentsRepository;
    }

    @Transactional
    public JiraServerTicket save(JiraServerTicket jiraServerTicket) {
        logger.info("Saving ticket...");
            JiraServerTicket ticketExists = jiraServerTicketRepository.findById(jiraServerTicket.getId()).orElse(null);
            Fields fields = jiraServerTicket.getFields();
            Resolution resolution = fields.getResolution();
            Resolution resolutionExists = resolutionRepository.findById(resolution.getId()).orElse(null);
            if (resolutionExists == null) {
                resolutionRepository.save(resolution);
            }
            JiraUser assignee = fields.getAssignee();
            if (assignee != null) {
                JiraUser assigneeExists = jiraUserRepository.findByKey(assignee.getKey()).orElse(null);
                if (assigneeExists == null) {
                    jiraUserRepository.save(assignee);
                }
            }
            JiraUser reporter = fields.getReporter();
            JiraUser reporterExists = jiraUserRepository.findByKey(reporter.getKey()).orElse(null);
            if (reporterExists == null) {
                jiraUserRepository.save(reporter);
            }
            JiraUser creator = fields.getCreator();
            JiraUser creatorExists = jiraUserRepository.findByKey(creator.getKey()).orElse(null);
            if (creatorExists == null) {
                jiraUserRepository.save(creator);
            }
            Status status = fields.getStatus();
            StatusCategory statusCategory = status.getStatusCategory();
            StatusCategory statusCategoryExists = statusCategoryRepository.findById(statusCategory.getId()).orElse(null);
            if (statusCategoryExists == null) {
                statusCategoryRepository.save(statusCategory);
            }
            Status statusExists = statusRepository.findById(status.getId()).orElse(null);
            if (statusExists == null) {
                statusRepository.save(status);
            }
            IssueType issueType = fields.getIssuetype();
            IssueType issueTypeExists = issueTypeRepository.findById(issueType.getId()).orElse(null);
            if (issueTypeExists == null) {
                issueTypeRepository.save(issueType);
            }

            Project project = fields.getProject();
            ProjectCategory projectCategory = project.getProjectCategory();
            if (projectCategory != null) {
                ProjectCategory projectCategoryExists = projectCategoryRepository.findById(projectCategory.getId()).orElse(null);
                if (projectCategoryExists == null) {
                    projectCategoryRepository.save(projectCategory);
                }
            }

            Project projectExists = projectRepository.findById(project.getId()).orElse(null);
            if (projectExists == null) {
                projectRepository.save(project);
            }

            Comment comment = fields.getComment();
            List<Comments> comments = comment.getComments();
            for (Comments c : comments) {
                JiraUser author = c.getAuthor();
                if (author != null) {
                    JiraUser authorExists = jiraUserRepository.findByKey(author.getKey()).orElse(null);
                    if (authorExists == null) {
                    jiraUserRepository.save(author);
                    }
                }
            }
        JiraServerTicket savedTicket = jiraServerTicketRepository.save(jiraServerTicket);
            Fields savedFields = savedTicket.getFields();
            Comment savedComment = savedFields.getComment();
            List<Comments> savedComments = savedComment.getComments();
            for (Comments c : savedComments) {
                c.setComment(savedComment);
                commentsRepository.save(c);
            }

            return savedTicket;
    }

    public List<JiraServerTicket> getLatestResolvedTickets(Integer maxResults) {
        return jiraServerTicketRepository.findLatestResolvedTickets(maxResults);
    }

    public List<JiraServerTicket> getLatestViewedTickets(int ticketsPerPage) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer userId = userDetails.getId();
        List<ViewedTicket> viewedTickets = viewedTicketsRepository.getLatestViewedTickets(userId, ticketsPerPage+1);
        List<JiraServerTicket> ticketArrayList= new ArrayList<>();
        for (ViewedTicket ticket : viewedTickets) {
            ticketArrayList.add(ticket.getTicket());
        }
        return ticketArrayList;
    }

    public void saveViewedTicket(String ticketId) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDateTime localDateTime = LocalDateTime.now();

        ZoneId currentZone = ZoneId.systemDefault(); // Get the current time zone
        ZonedDateTime zonedDateTime = localDateTime.atZone(currentZone);
        JiraServerTicket ticket = jiraServerTicketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            return;
        }
        ViewedTicket viewedTicketExists = viewedTicketsRepository.findByTicketAndUser(ticket.getId(), userDetails.getId()).orElse(null);
        if (viewedTicketExists != null) {
            viewedTicketExists.setViewedAt(zonedDateTime);
            viewedTicketsRepository.save(viewedTicketExists);
            return;
        }
        ViewedTicket viewedTicket = new ViewedTicket(null, ticket, userDetails, zonedDateTime);
        viewedTicketsRepository.save(viewedTicket);
    }

    public JiraServerTicket getTicketById(String ticketId) {
        return jiraServerTicketRepository.findById(ticketId).orElse(null);
    }

    public List<JiraServerTicket> getTicketsByIds(List<String> ticketIds) {
        List<JiraServerTicket> returnedTickets = jiraServerTicketRepository.findJiraServerTicketsByIds(ticketIds);
        if (returnedTickets == null || returnedTickets.isEmpty()) {
            return null;
        }
        List<JiraServerTicket> sortedTickets = new ArrayList<>();
        Map<String, JiraServerTicket> ticketMap = new HashMap<>();
        for(JiraServerTicket t: returnedTickets) {
            ticketMap.put(t.getId(), t);
        }

        for (String id : ticketIds) {
            if (ticketMap.containsKey(id)){
                sortedTickets.add(ticketMap.get(id));
            }

        }
        return sortedTickets;
    }

}
