package com.example.stacktradeapp.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


@Configuration
@ComponentScan(basePackages = {"com.example.stacktradeapp.elasticsearch","com.example.stacktradeapp.entities"})
@EnableElasticsearchRepositories(basePackages = "com.example.stacktradeapp.elasticsearch.repository")
public class ElasticsearchConfig {
    private final Environment environment;


    @Autowired
    public ElasticsearchConfig(Environment environment) {
        this.environment = environment;
    }

    //configure URL and port in which Elasticsearch is running.
    @Bean
    public RestClient getRestClient(){
        return RestClient.builder(
                new HttpHost(environment.getRequiredProperty("elasticsearch.host"),
                        environment.getRequiredProperty("elasticsearch.port", Integer.class))).build();
    }

    //returns the Transport Object, whose purpose is it automatically map the Model Class to JSON and integrates them with API Client.
    @Bean
    public ElasticsearchTransport getElasticsearchTransport(){
        return new RestClientTransport(getRestClient(),new JacksonJsonpMapper());
    }

    //returns a bean of elasticsearchclient, which we further use to perform all query operation with Elasticsearch.
    @Bean
    public ElasticsearchClient elasticsearchClient() {

        // Create the transport with a Jackson mapper
        return new ElasticsearchClient(getElasticsearchTransport());
    }
}