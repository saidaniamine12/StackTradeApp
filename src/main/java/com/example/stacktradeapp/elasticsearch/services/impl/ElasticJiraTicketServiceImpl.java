package com.example.stacktradeapp.elasticsearch.services.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.stacktradeapp.elasticsearch.models.ElasticResponseEntity;
import com.example.stacktradeapp.elasticsearch.models.JiraTicket;
import com.example.stacktradeapp.elasticsearch.services.ElasticJiraTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;


@Service
public class ElasticJiraTicketServiceImpl implements ElasticJiraTicketService {

    Logger logger = LoggerFactory.getLogger(ElasticJiraTicketServiceImpl.class);


    @Value("${com.example.stacktradeapp.elasticsearch.indexName}")
    private String indexName;

    //create a new instance of the elasticsearch client
    private final ElasticsearchClient elasticsearchClient;

    //inject the elasticsearch client into the constructor
    @Autowired
    public ElasticJiraTicketServiceImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    //get tickets that match the search text
    @Override
    public ElasticResponseEntity textSearchQuery(String searchText, Integer pageNumber, Integer ticketsPerPage){

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

        try {
            //search for the query in the summary and description fields
            SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                            .index(indexName)
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
                return new ElasticResponseEntity(mapResponseToJiraTicketList(response),response.hits().total().value()) ;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //get the latest created tickets
    @Override
    public ElasticResponseEntity getLatestCreatedTickets(Integer pageNumber,Integer ticketsPerPage) {

        //if skip reached the end of the index
        boolean reachedTheEnd = false;
        //calculate the number of documents to skip
        int skip = (pageNumber - 1) * ticketsPerPage;


        try {
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

            Integer finalSkip = skip;
            logger.info("skip: " + skip);

            SearchResponse<JiraTicket> response = elasticsearchClient.search(s -> s
                            .index(indexName)
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


        } catch (IOException e) {
            logger.info("error: " + e.getMessage());
        }

        return null;

    }

    @Override
    public Long getIndexSize() throws IOException{
            return  elasticsearchClient.count(
                    c -> c.index(indexName)
            ).count();

    }

    @Override
    public JiraTicket getTicketById(String id) {
        try {
            //get the ticket by id from the index
            GetResponse<JiraTicket> response = elasticsearchClient.get(g -> g
                            .index(indexName)
                            .id(id),
                    JiraTicket.class
            );

            if (response.source() != null) {
                logger.info("response: JiraTicket with id " + response.source().getId() + " was fetched");
                return new JiraTicket(response.source());
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
