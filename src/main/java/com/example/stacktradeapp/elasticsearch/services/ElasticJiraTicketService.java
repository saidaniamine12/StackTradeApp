package com.example.stacktradeapp.elasticsearch.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.stacktradeapp.elasticsearch.documents.JiraTicket;
import com.example.stacktradeapp.elasticsearch.repository.JiraTicketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.example.stacktradeapp.StackTradeAppApplication.logger;

@Service
public class ElasticJiraTicketService {
    private final JiraTicketRepository jiraTicketRepository;

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticJiraTicketService(JiraTicketRepository jiraTicketRepository, ElasticsearchClient elasticsearchClient) {
        this.jiraTicketRepository = jiraTicketRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    public JiraTicket getTicketById(String id){
        Optional<JiraTicket> optionalJiraTicket = Optional.ofNullable(jiraTicketRepository.findJiraTicketById(id));
        return optionalJiraTicket.orElse(null);
    }

    public Map<String,Double> textSearchQuery(String searchText) throws IOException {
        //fieldsList
        List<String> fieldsList = new ArrayList<>();
        fieldsList.add("summary^2");
        fieldsList.add("description");


        Query mustBeInSummary = MatchQuery.of(m -> m
                .field("summary")
                .query(searchText)
        )._toQuery();

        Query shouldBeIndescription = MatchQuery.of(m -> m
                .field("description")
                .query(searchText)
        )._toQuery();
        SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                        .index("spring_jira_ticket_index")
                        .query(q -> q
                                .bool(t -> t
                                        .must(mustBeInSummary)
                                        .should(shouldBeIndescription)
                                )
                        ).size(50),
                JiraTicket.class
        );




        TotalHits total = response.hits().total();
        if (total != null) {
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

            if (isExactResult) {
                logger.info("There are " + total.value() + " results");
            } else {
                logger.info("There are more than " + total.value() + " results");
            }
            List<String> ids = new ArrayList<>();


            List<Hit<JiraTicket>> hits = response.hits().hits();
            Map<String,Double> jiraIdsScoreMap = new HashMap<>();
            for (Hit<JiraTicket> hit : hits) {
                JiraTicket jiraTicket = hit.source();

                if (jiraTicket != null){
                    ids.add(jiraTicket.getId());
                    jiraIdsScoreMap.put(jiraTicket.getId(), hit.score());
                    logger.info("returned ticket: "+ jiraTicket);
                }

            }
            logger.info("returned map: "+ jiraIdsScoreMap);
            return jiraIdsScoreMap;
        } else {
            return null;
        }
    }



}
