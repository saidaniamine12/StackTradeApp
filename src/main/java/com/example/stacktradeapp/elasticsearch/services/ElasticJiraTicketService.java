package com.example.stacktradeapp.elasticsearch.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.stacktradeapp.elasticsearch.documents.JiraTicket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.example.stacktradeapp.StackTradeAppApplication.logger;

@Service
public class ElasticJiraTicketService {

    //create a new instance of the elasticsearch client
    private final ElasticsearchClient elasticsearchClient;

    //inject the elasticsearch client into the constructor
    @Autowired
    public ElasticJiraTicketService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    //get tickets that match the search text
    public List<JiraTicket> textSearchQuery(String searchText) throws IOException {
        //create a match query for the summary field
        Query mustBeInSummary = MatchQuery.of(m -> m
                .field("summary")
                .query(searchText)
        )._toQuery();
        //create a match query for the description field
        Query shouldBeIndescription = MatchQuery.of(m -> m
                .field("description")
                .query(searchText)
        )._toQuery();
        //search for the query in the summary and description fields
        SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                        .index("spring_jira_index")
                        .query(q -> q
                                .bool(t -> t
                                        .must(mustBeInSummary)
                                        .should(shouldBeIndescription)
                                )
                        ).size(50),
                JiraTicket.class
        );



        //get the total number of results
        TotalHits total = response.hits().total();
        if (total != null) {
            //check if the total number of results is exact or a lower bound
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

            //print the total number of results
            if (isExactResult) {
                logger.info("There are " + total.value() + " results");
            } else {
                logger.info("There are more than " + total.value() + " results");
            }
            //get the results
            List<Hit<JiraTicket>> hits = response.hits().hits();
            //create a list of JiraTickets
            List<JiraTicket> jiraTickets = new ArrayList<>();

            //add the JiraTickets to the list
            for (Hit<JiraTicket> hit : hits) {
                //get the JiraTicket
                JiraTicket jiraTicket = hit.source();
                //add the JiraTicket to the list
                if (jiraTicket != null){
                    jiraTickets.add(jiraTicket);
                    logger.info("returned ticket: "+ jiraTicket.getId());
                    logger.info("returned ticket: "+ jiraTicket.getCreated());
                }

            }
            //return the list of JiraTickets
            return jiraTickets;
        } else {
            //return null if there are no results
            return null;
        }
    }

    //get the latest created tickets
    public List<JiraTicket> getLatestCreatedTickets() throws IOException {
        //search for the latest created tickets
        SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                        .index("spring_jira_index")
                        .query(q -> q
                                .matchAll(builder -> builder)
                        ).sort(sorted -> sorted
                                .field(x -> x
                                        .field("created")
                                        .order(SortOrder.Desc)
                                )
                        ).size(50),
                JiraTicket.class
        );

        //get the total number of results
        TotalHits total = response.hits().total();
        if (total != null) {
            //check if the total number of results is exact or a lower bound
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

            //print the total number of results
            if (isExactResult) {
                logger.info("There are " + total.value() + " results");
            } else {
                logger.info("There are more than " + total.value() + " results");
            }
            //get the results
            List<Hit<JiraTicket>> hits = response.hits().hits();
            //create a list of JiraTickets
            List<JiraTicket> jiraTickets = new ArrayList<>();

            //add the JiraTickets to the list
            for (Hit<JiraTicket> hit : hits) {
                //get the JiraTicket
                JiraTicket jiraTicket = hit.source();
                //add the JiraTicket to the list
                if (jiraTicket != null){
                    jiraTickets.add(jiraTicket);
                    logger.info("returned ticket: "+ jiraTicket.getId());
                    logger.info("returned ticket: "+ jiraTicket.getCreated());
                }

            }
            //return the list of JiraTickets
            return jiraTickets;
        } else {
            //return null if there are no results
            return null;
        }
    }



}
