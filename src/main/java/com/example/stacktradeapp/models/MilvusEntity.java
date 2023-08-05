package com.example.stacktradeapp.models;

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
            for (float f : normalizeVector(this.vector)) {
                summaryVector.add(f);
            }
            return summaryVector;
        }
        for (float f : this.vector) {
            summaryVector.add(f);
        }
        return summaryVector;
    }

    public static float[] normalizeVector(float[] in) {
        float[] out = new float[in.length];
        float sum = 0;
        for (float v : in) {
            sum += v * v;
        }
        sum = (float) Math.sqrt(sum);
        for (int i = 0; i < in.length; i++)
            out[i] = in[i] / sum;
        return out;
    }

}
