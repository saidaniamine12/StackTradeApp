package com.example.stacktradeapp.controllers.Impl;

import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsResponse;
import com.example.stacktradeapp.controllers.ElasticSettingsController;
import com.example.stacktradeapp.elasticsearch.models.ElasticsearchSettings;
import com.example.stacktradeapp.elasticsearch.services.ElasticsearchSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ElasticSettingsControllerImpl implements ElasticSettingsController {

    private final ElasticsearchSettingsService elasticsearchSettingsService;

    public ElasticSettingsControllerImpl(ElasticsearchSettingsService elasticsearchSettingsService) {
        this.elasticsearchSettingsService = elasticsearchSettingsService;
    }

    @Override
    public ResponseEntity<String> getElasticsearchSettings(){
        try {
            ElasticsearchSettings elasticsearchSettings = new ElasticsearchSettings(elasticsearchSettingsService.getMaxResultWindow());
            if(elasticsearchSettings.getMaxResultWindow() != 0) return ResponseEntity.ok(elasticsearchSettings.toString());
            return ResponseEntity.badRequest().build();

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> setElasticsearchSettings() {
        try {
            PutIndicesSettingsResponse settingsResponse= elasticsearchSettingsService.setMaxResultWindow();
            return ResponseEntity.ok(settingsResponse.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
