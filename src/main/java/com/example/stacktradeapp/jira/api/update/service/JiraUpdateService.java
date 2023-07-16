package com.example.stacktradeapp.jira.api.update.service;

import com.example.stacktradeapp.exception.DocumentParsingException;
import com.example.stacktradeapp.milvus.vectorRepository.MilvusRepository;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.Fields;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import com.example.stacktradeapp.sentenceTransformers.SentenceTransformerService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


//get latest tickets from jira and update mongodb and milvus db with new tickets
@Service
public class JiraUpdateService {



    @Value("${com.example.stacktradeapp.milvus.summary.collection.name}")
    private String summaryCollectionName;

    @Value("${com.example.stacktradeapp.milvus.collections.id.field.name}")
    private String summaryCollectionIdFieldName;

    @Value("${com.example.stacktradeapp.milvus.summary.collection.vector.field.name}")
    private String summaryCollectionVectorFieldName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.name}")
    private String descriptionCollectionName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.id.field.name}")
    private String descriptionIdFieldName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.vector.field.name}")
    private String descriptionCollectionVectorFieldName;

    private final Logger logger = LoggerFactory.getLogger(JiraUpdateService.class);
    private static final String JIRA_API_URL = "https://jira.atlassian.com/rest/api/latest/search";
    final String personalAccessToken = "NzE5MTI5MTAxOTg4OnTeKdBf1h9kmceiiUl3Kx+PdKF0";
    final String jqlQuery = "issuetype = Bug AND resolution = Fixed AND resolved >= -500d ORDER BY updated ASC";
    JsonNodeFactory jnf = JsonNodeFactory.instance;
    private final HttpClient httpClient;
    private final MilvusRepository milvusRepository;
    private final SentenceTransformerService sentenceTransformerService;



    public JiraUpdateService(MilvusRepository milvusRepository, SentenceTransformerService sentenceTransformerService) {
        this.httpClient = HttpClient.newHttpClient();
        this.milvusRepository = milvusRepository;
        this.sentenceTransformerService = sentenceTransformerService;
    }



    public List<JiraServerTicket> getLatestTicketsFromJiraServer(){
        ObjectNode payload = jnf.objectNode();
        {
            ArrayNode fields = payload.putArray("fields");
            fields.add("key");
            fields.add("summary");
            fields.add("assignee");
            fields.add("reporter");
            fields.add("creator");
            fields.add("issuetype");
            fields.add("updated");
            fields.add("description");
            fields.add("resolution");
            fields.add("resolutiondate");
            fields.add("created");
            fields.add("project");
            fields.add("comment");
            fields.add("status");
            payload.put("jql", jqlQuery);
            payload.put("maxResults", 10000);
            payload.put("startAt", 0);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .uri(URI.create(JIRA_API_URL))
                .header("Authorization", "Bearer " + personalAccessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Error while getting tickets from Jira server. Status code: {}", response.statusCode());
                return null;
            }


            JSONObject jsonObject = new JSONObject(response.body());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.registerModule(new JavaTimeModule());
            // Assuming you have the JSONArray "issues" as a JsonNode
            JsonNode issuesNode = objectMapper.readTree(jsonObject.toString()).get("issues");
            // Map the JSON array to a List<JiraTicket>
            List<JiraServerTicket> jiraTicketsList = objectMapper.readValue(
                    issuesNode.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, JiraServerTicket.class)
            );

            logger.info("Found {} new issues", jiraTicketsList.size());
            return jiraTicketsList;
        } catch (IOException ex) {
            logger.error("IOException: ", ex);
        } catch (InterruptedException ex) {
            logger.error("InterruptedException: ", ex);
        }
        return null;
    }


    public void insertTicketsIntoMilvusCollection(List<JiraServerTicket> tickets) throws JSONException, DocumentParsingException {
        if (tickets.size() == 0) {
            logger.info("No new tickets found");
            return;
        }

        //convert the search entities to list of milvus entities to be indexed into milvus
        for (JiraServerTicket ticket : tickets) {
            Fields fields = ticket.getFields();
            String summary = fields.getSummary();
            String description = fields.getDescription();
            Long id = Long.parseLong(ticket.getId()) ;
            List<Float> summaryEmbedding = sentenceTransformerService.generateSymmetricEmbedding(summary);

            List<Float> descriptionEmbedding = sentenceTransformerService.generateAsymmetricEmbedding(description);

            milvusRepository.insertDocument(summaryCollectionName,summaryCollectionIdFieldName, id,summaryCollectionVectorFieldName,summaryEmbedding);
            milvusRepository.insertDocument(descriptionCollectionName,descriptionIdFieldName, id,descriptionCollectionVectorFieldName,descriptionEmbedding);
        }
        logger.info("Inserted {} new tickets into milvus", tickets.size());
        milvusRepository.flush(summaryCollectionName);
        milvusRepository.flush(descriptionCollectionName);


    }


}
