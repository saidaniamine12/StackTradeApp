package com.example.stacktradeapp.jira.api.update.service;

import com.example.stacktradeapp.exception.DocumentParsingException;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ScheduledUpdateJob {


    private final Logger logger = LoggerFactory.getLogger(ScheduledUpdateJob.class);

    final JiraUpdateService jiraUpdateService;

    public ScheduledUpdateJob(JiraUpdateService jiraUpdateService) {
        this.jiraUpdateService = jiraUpdateService;
    }

    @Scheduled(cron = "10 * * * * *") // At 11 PM every day
    public void updateJiraTickets() throws DocumentParsingException {

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);
        logger.info("Updating Jira tickets at: " + formattedDate);
        JSONArray  jiraTickets = jiraUpdateService.getLatestTicketsFromJiraServer();

        try {
            jiraUpdateService.insertJSONArrayTicketsIntoMongoDB(jiraTickets);
        } catch (Exception e) {
            logger.error("Error inserting some documents into mongodb: " + e.getMessage());
        }

        jiraUpdateService.insertJSONArrayTicketsIntoMilvusCollections(jiraTickets);

    }
}
