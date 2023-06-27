package com.example.stacktradeapp.entities;

import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Data
@EntityScan
public class SimpleUserEntity {
    private String name;
    private String emailAdress;

    public SimpleUserEntity(String creatorName, String creatorEmailAddress) {
    }
}
