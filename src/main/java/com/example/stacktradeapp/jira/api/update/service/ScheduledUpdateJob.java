package com.example.stacktradeapp.jira.api.update.service;

import com.example.stacktradeapp.exception.DocumentParsingException;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import com.example.stacktradeapp.services.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ScheduledUpdateJob {


    private final Logger logger = LoggerFactory.getLogger(ScheduledUpdateJob.class);

    final JiraUpdateService jiraUpdateService;

    private final TicketService ticketService;

    public ScheduledUpdateJob(JiraUpdateService jiraUpdateService, TicketService ticketService) {
        this.jiraUpdateService = jiraUpdateService;
        this.ticketService = ticketService;
    }

    @Scheduled(cron = "0 0 23 * * *") // (0 0 23 * * *) At 11 PM every day
    public void updateJiraTickets() throws DocumentParsingException {

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);
        logger.info("Updating Jira tickets at: " + formattedDate);
        List<JiraServerTicket> jiraTickets = jiraUpdateService.getLatestTicketsFromJiraServer();
        for (JiraServerTicket ticket : jiraTickets) {
            JiraServerTicket insertedTicket = ticketService.save(ticket);
            if (insertedTicket == null) {
                logger.error("Error saving ticket: " + ticket.getKey());
                jiraTickets.remove(ticket);
                continue;
            }
            logger.info("Saved ticket: " + ticket.getKey());


        }
        jiraUpdateService.insertTicketsIntoMilvusCollection(jiraTickets);

    }
}
