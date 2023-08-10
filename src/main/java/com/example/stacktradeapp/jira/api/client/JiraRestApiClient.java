package com.example.stacktradeapp.jira.api.client;

import java.net.http.HttpClient;

public class JiraRestApiClient {
    private static final String JIRA_API_URL = "https://your-jira-instance.atlassian.net/rest/api/2";

    private HttpClient httpClient;

    public JiraRestApiClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

}
