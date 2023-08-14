package com.example.stacktradeapp.jira.api.update.service;

import com.example.stacktradeapp.exception.DocumentParsingException;
import com.example.stacktradeapp.milvus.vectorRepository.MilvusRepository;
import com.example.stacktradeapp.models.simpleTicketPOJO;
import com.example.stacktradeapp.mongodb.services.MongoJiraTicketService;
import com.example.stacktradeapp.sentenceTransformers.SentenceTransformerService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.json.JSONArray;
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
    private String summaryIdFieldName;

    @Value("${com.example.stacktradeapp.milvus.summary.collection.vector.field.name}")
    private String summaryCollectionVectorFieldName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.name}")
    private String descriptionCollectionName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.vector.field.name}")
    private String descriptionIdFieldName;

    @Value("${com.example.stacktradeapp.milvus.description.collection.vector.field.name}")
    private String descriptionCollectionVectorFieldName;

    private final Logger logger = LoggerFactory.getLogger(JiraUpdateService.class);
    private static final String JIRA_API_URL = "https://jira.atlassian.com/rest/api/latest/search";
    final String personalAccessToken = "NzE5MTI5MTAxOTg4OnTeKdBf1h9kmceiiUl3Kx+PdKF0";
    final String jqlQuery = "issuetype = Bug AND resolution = Fixed AND resolved >= -5d ORDER BY updated ASC";
    JsonNodeFactory jnf = JsonNodeFactory.instance;
    private final HttpClient httpClient;
    private final MongoJiraTicketService mongoJiraTicketService;
    private final MilvusRepository milvusRepository;
    private final SentenceTransformerService sentenceTransformerService;


    public JiraUpdateService(MongoJiraTicketService mongoJiraTicketService, MilvusRepository milvusRepository, SentenceTransformerService sentenceTransformerService) {
        this.mongoJiraTicketService = mongoJiraTicketService;
        this.httpClient = HttpClient.newHttpClient();
        this.milvusRepository = milvusRepository;
        this.sentenceTransformerService = sentenceTransformerService;
    }



    public JSONArray getLatestTicketsFromJiraServer(){
        ObjectNode payload = jnf.objectNode();
        {
            ArrayNode fields = payload.putArray("fields");
            fields.add("summary");
            fields.add("assignee");
            fields.add("reporter");
            fields.add("description");
            fields.add("resolution");
            fields.add("created");
            fields.add("project");
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
            JSONArray issues =  jsonObject.getJSONArray("issues");
            if (issues.length() == 0) {
                logger.info("No new issues found");
                return null;
            }
            logger.info("Found {} new issues", issues.length());
            return issues;
        } catch (IOException ex) {
            logger.error("IOException: ", ex);
        } catch (InterruptedException ex) {
            logger.error("InterruptedException: ", ex);
        }
        return null;
    }

    public void insertJSONArrayTicketsIntoMongoDB(JSONArray issues) {
        if (issues.length() == 0) {
            logger.info("No new issues found");
            return;
        }
        List<Document> docs = new ArrayList<>();
        for (Object json : issues) {
            Document doc = Document.parse(json.toString());
            docs.add(doc);
        }
        mongoJiraTicketService.insertTickets(docs);
    }

    public void insertJSONArrayTicketsIntoMilvusCollections(JSONArray issues) throws JSONException, DocumentParsingException {
        List<String> ticketIds = new ArrayList<>();
        if (issues.length() == 0) {
            logger.info("No new issues found");
            return;
        }

        //get the ids of the tickets
        for (Object json : issues) {
            Document doc = Document.parse(json.toString());
            String id = doc.get("id").toString();
            ticketIds.add(id);
        }

        //get the tickets from mongodb
        List<BasicDBObject> returnedTickets = mongoJiraTicketService.getTicketsByIds(ticketIds);

        //convert the tickets to search entities
        List<simpleTicketPOJO> searchEntities = simpleTicketPOJO.basicDocToTicketPOJO(returnedTickets);

        boolean isSummaryLoaded = milvusRepository.loadCollectionToMemory("spring_jira_summary_Collection");
        if (!isSummaryLoaded) {
            logger.error("Collection is not loaded to memory");
            return;
        }
        boolean isDescriptionLoaded = milvusRepository.loadCollectionToMemory("spring_jira_description_Collection");
        if (!isDescriptionLoaded ) {
            logger.error("Collection is not loaded to memory");
            return;
        }
        //convert the search entities to list of milvus entities to be indexed into milvus
        for (simpleTicketPOJO simpleTicketPOJO : searchEntities) {
            String summary = simpleTicketPOJO.getSummary();
            String description = simpleTicketPOJO.getDescription();
            Long id = Long.parseLong(simpleTicketPOJO.getId()) ;
            List<Float> summaryEmbedding = sentenceTransformerService.generateSymmetricEmbedding(summary);

            List<Float> descriptionEmbedding = sentenceTransformerService.generateAsymmetricEmbedding(description);

            milvusRepository.insertDocument(summaryCollectionName,summaryIdFieldName, id,summaryCollectionVectorFieldName,summaryEmbedding);
            milvusRepository.insertDocument(descriptionCollectionName,descriptionIdFieldName, id,descriptionCollectionVectorFieldName,descriptionEmbedding);
        }
        System.out.println("flushing");
        milvusRepository.flush(summaryCollectionName);
        milvusRepository.flush(descriptionCollectionName);


    }


}
