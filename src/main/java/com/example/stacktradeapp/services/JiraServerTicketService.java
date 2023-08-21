package com.example.stacktradeapp.services;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.*;
import com.example.stacktradeapp.repositories.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JiraServerTicketService {

    private static final Logger logger = LoggerFactory.getLogger(JiraServerTicketService.class);

    private final JiraServerTicketRepository jiraServerTicketRepository;
    private final ResolutionRepository resolutionRepository;
    private final StatusCategoryRepository statusCategoryRepository;
    private final StatusRepository statusRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final JiraUserRepository jiraUserRepository;

    @Autowired
    public JiraServerTicketService(JiraServerTicketRepository jiraServerTicketRepository,
                                   ResolutionRepository resolutionRepository,
                                   StatusCategoryRepository statusCategoryRepository,
                                   StatusRepository statusRepository,
                                   ProjectRepository projectRepository,
                                   ProjectCategoryRepository projectCategoryRepository,
                                   IssueTypeRepository issueTypeRepository,
                                   JiraUserRepository jiraUserRepository) {
        this.jiraServerTicketRepository = jiraServerTicketRepository;
        this.resolutionRepository = resolutionRepository;
        this.statusCategoryRepository = statusCategoryRepository;
        this.statusRepository = statusRepository;
        this.projectRepository = projectRepository;
        this.projectCategoryRepository = projectCategoryRepository;
        this.issueTypeRepository = issueTypeRepository;
        this.jiraUserRepository = jiraUserRepository;
    }

    @Transactional
    public JiraServerTicket save(JiraServerTicket jiraServerTicket) {
        logger.info("Saving ticket...");

        try {
            JiraServerTicket ticketExists = jiraServerTicketRepository.findById(jiraServerTicket.getId()).orElse(null);
            if (ticketExists != null) {
                return null;
            }
            System.out.println(jiraServerTicket);
            Fields fields = jiraServerTicket.getFields();
            System.out.println(fields);
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

            CommentSection commentSection = fields.getCommentSection();
            if (commentSection != null) {
                List<Comments> comments = commentSection.getComments();
                for (Comments c : comments) {
                    JiraUser author = c.getAuthor();
                    JiraUser authorExists = jiraUserRepository.findByKey(author.getKey()).orElse(null);
                    if (authorExists == null) {
                        jiraUserRepository.save(author);
                    }
                }

            }

            JiraServerTicket ticket = jiraServerTicketRepository.save(jiraServerTicket);
            return jiraServerTicketRepository.save(ticket);
        } catch (Exception e) {
            logger.error("Error while saving ticket: {}", e.getMessage());
            return null;
        }

    }

    public List<JiraServerTicket> getLatestResolvedTickets(Integer maxResults) {
        return jiraServerTicketRepository.findLatestResolvedTickets(maxResults);
    }
}
