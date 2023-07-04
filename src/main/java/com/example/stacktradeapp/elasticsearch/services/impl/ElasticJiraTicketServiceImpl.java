package com.example.stacktradeapp.elasticsearch.services.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.stacktradeapp.elasticsearch.entities.ElasticResponseEntity;
import com.example.stacktradeapp.elasticsearch.entities.JiraTicket;
import com.example.stacktradeapp.elasticsearch.services.ElasticJiraTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

import static com.example.stacktradeapp.StackTradeAppApplication.logger;

@Service
public class ElasticJiraTicketServiceImpl implements ElasticJiraTicketService {

    //create a new instance of the elasticsearch client
    private final ElasticsearchClient elasticsearchClient;

    //inject the elasticsearch client into the constructor
    @Autowired
    public ElasticJiraTicketServiceImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    //get tickets that match the search text
    @Override
    public ElasticResponseEntity textSearchQuery(String searchText, Integer pageNumber, Integer ticketsPerPage) throws IOException{

        //calculate the number of documents to skip
        int skip = (pageNumber - 1) * ticketsPerPage;

        //create a match query for the summary field
        Query mustBeInSummary = MatchQuery.of(m -> m
                .field("summary")
                .query(searchText)
        )._toQuery();

        //create a match query for the description field
        Query shouldBeInDescription = MatchQuery.of(m -> m
                .field("description")
                .query(searchText)
        )._toQuery();

        //search for the query in the summary and description fields
        SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                        .index("spring_jira_index")
                        .query(q -> q
                                .bool(t -> t
                                        .must(mustBeInSummary)
                                        .should(shouldBeInDescription)
                                )
                        ).from(skip).size(ticketsPerPage),
                JiraTicket.class
        );

        //return the list of JiraTickets
        if (response.hits().total() != null) {
            logger.info("response: " + response.hits().hits());
            return new ElasticResponseEntity(mapResponseToJiraTicketList(response),response.hits().total().value()) ;
        }
        return null;

    }

    //get the latest created tickets
    @Override
    public ElasticResponseEntity getLatestCreatedTickets(Integer pageNumber,Integer ticketsPerPage) throws IOException {

        //if skip reached the end of the index
        boolean reachedTheEnd = false;
        //calculate the number of documents to skip
        int skip = (pageNumber - 1) * ticketsPerPage;
        //get the total number of documents in the index
        Integer indexSize = Math.toIntExact(getIndexSize());

        //check if skip is greater than the index size
        if (skip > indexSize - ticketsPerPage) {
            logger.info("skip is greater than index size");
            //update skip to the last page
            skip = indexSize - ticketsPerPage;
            //set reachedTheEnd to true
            reachedTheEnd = true;
        }

        //search for the latest created tickets
        Integer finalSkip = skip;
        SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                        .index("spring_jira_index")
                        .query(q -> q
                                .matchAll(builder -> builder)
                        ).sort(sorted -> sorted
                                .field(x -> x
                                        .field("created")
                                        .order(SortOrder.Desc)
                                )
                        ).from(finalSkip)
                        .size(ticketsPerPage)
                        ,
                JiraTicket.class
        );


        //get the total number of results
        List<JiraTicket> jiraTickets = mapResponseToJiraTicketList(response);
        Long totalHits = getIndexSize();
        //check if skip reached the end of the index
        if(reachedTheEnd){
            int listSize = jiraTickets.size();
            int lastDigit = listSize - indexSize % ticketsPerPage;
            return new ElasticResponseEntity(jiraTickets.subList(lastDigit,listSize),totalHits) ;
        }
        //return the list of JiraTickets
        return new ElasticResponseEntity(jiraTickets, totalHits);
    }

    @Override
    public Long getIndexSize() throws IOException {
        return  elasticsearchClient.count(
                c -> c.index("spring_jira_index")
        ).count();
    }


    //response to jira ticket
    List<JiraTicket> mapResponseToJiraTicketList(SearchResponse<JiraTicket> response) {
        TotalHits total = response.hits().total();
        List<JiraTicket> jiraTickets = new ArrayList<>();
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

            //add the JiraTickets to the list
            for (Hit<JiraTicket> hit : hits) {
                //get the JiraTicket
                JiraTicket jiraTicket = hit.source();
                //add the JiraTicket to the list
                if (jiraTicket != null) {
                    jiraTickets.add(jiraTicket);
                }

            }

        }
        //return the list of JiraTickets if it is not empty
        if (jiraTickets.size() > 0) {
            return jiraTickets;
        } else {
            return null;
        }
    }


}
