package com.example.stacktradeapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.List;

@Data
@EntityScan
@AllArgsConstructor
public class SearchResponse {
    List<SearchEntity> searchEntities;
    Long totalHits;
}
