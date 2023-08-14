package com.example.stacktradeapp.models;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
public class MilvusEntity {
    private final Long id;
    private final List<Float> vector;

    public MilvusEntity(Long id,List<Float> vector) {
        this.id = id;
        this.vector = vector;
    }

    public Long getId() {
        return this.id;
    }



}
