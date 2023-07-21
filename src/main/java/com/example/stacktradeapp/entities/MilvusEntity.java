package com.example.stacktradeapp.entities;

import com.example.stacktradeapp.models.SentenceTransformer;

import java.util.ArrayList;
import java.util.List;

public class MilvusEntity {
    private final Long id;
    private final float[] vector;

    public MilvusEntity(Long id, float[] vector) {
        this.id = id;
        this.vector = vector;

    }

    public Long getId() {
        return this.id;
    }

    public List<Float> getVector(boolean toNormalize) {
        List<Float> summaryVector = new ArrayList<>();
        if (toNormalize) {
            for (float f : SentenceTransformer.normalizeVector(this.vector)) {
                summaryVector.add(f);
            }
            return summaryVector;
        }
        for (float f : this.vector) {
            summaryVector.add(f);
        }
        return summaryVector;
    }

}
