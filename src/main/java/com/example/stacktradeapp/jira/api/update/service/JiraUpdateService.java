package com.example.stacktradeapp.jira.api.update.service;

import com.example.stacktradeapp.milvus.vectorRepository.MilvusRepository;
import com.example.stacktradeapp.mongodb.services.MongoJiraTicketService;
import com.example.stacktradeapp.sentenceTransformers.SentenceTransformerService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


//get latest tickets from jira and update mongodb and milvus db with new tickets
@Service
public class JiraUpdateService {

    private final MongoJiraTicketService mongoJiraTicketService;


    private final MilvusRepository milvusRepository;

    private final SentenceTransformerService sentenceTransformerService;


    public JiraUpdateService(MongoJiraTicketService mongoJiraTicketService, MilvusRepository milvusRepository, SentenceTransformerService sentenceTransformerService) {
        this.mongoJiraTicketService = mongoJiraTicketService;

        this.milvusRepository = milvusRepository;
        this.sentenceTransformerService = sentenceTransformerService;
    }



    void getLatestFixedTicketsFromJira() throws IOException, InterruptedException, JSONException {
        String personalAccessToken = "NzE5MTI5MTAxOTg4OnTeKdBf1h9kmceiiUl3Kx+PdKF0";
        //configure the Http request to get the latest tickets from jira
        String ex = "https://jira.spring.io/rest/api/2/search?jql=project=DATAREST+AND+status=Done+AND+resolution=Fixed&maxResults=1000";
        String apiUrl = "https://jira.atlassian.com/rest/api/latest/issue/1430697";

        HttpClient httpClient = HttpClient.newHttpClient(); ;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + personalAccessToken)
                .build();
        //get latest tickets from jira

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject jsonObject = new JSONObject(response.body());
        List<JSONObject> ticketList = List.of(jsonObject);
        //save the full object to mongodb
        mongoJiraTicketService.insertTickets(ticketList);



        //update mongodb with new tickets
        //update milvus db with new tickets

    }


}
