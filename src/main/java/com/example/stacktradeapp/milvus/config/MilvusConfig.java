package com.example.stacktradeapp.milvus.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.example.stacktradeapp.milvus")
public class MilvusConfig {

    @Value("${com.example.stacktradeapp.milvus.host}")
    private String host;

    @Value("${com.example.stacktradeapp.milvus.port}")
    private int port ;

    @Bean
    public MilvusClient milvusClient() {
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .build()
        );
    }



}
