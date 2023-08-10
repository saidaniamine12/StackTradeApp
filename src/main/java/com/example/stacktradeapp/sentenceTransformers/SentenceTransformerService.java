package com.example.stacktradeapp.sentenceTransformers;

import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SentenceTransformerService {
    ZooModel<String, float[]> symmetricModel;
    Predictor<String, float[]> symmetricPredictor;
    ZooModel<String, float[]> asymmetricModel;
    Predictor<String, float[]> asymmetricPredictor;

    public SentenceTransformerService()  {
        super();
        try {
            Criteria<String, float[]> symmetricCriteria = createSymmetricCriteria();
            Criteria<String, float[]> asymmetricCriteria = createAsymmetricCriteria();
            this.symmetricModel = symmetricCriteria.loadModel();
            this.symmetricPredictor = symmetricModel.newPredictor();
            System.out.println("Symmetric Model loaded.");
            this.asymmetricModel = asymmetricCriteria.loadModel();
            this.asymmetricPredictor = asymmetricModel.newPredictor();
            System.out.println("Asymmetric Model loaded.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Embed the sentence using the symmetric model
    private List<Float> symmetricEmbed(String sentence) throws TranslateException {
        return this.floatArrayToFlaotList(normalizeVector(this.symmetricPredictor.predict(sentence)));
    }

    // Embed the sentence using the asymmetric model
    private List<Float> asymmetricEmbed(String sentence) throws TranslateException {
        return this.floatArrayToFlaotList(normalizeVector(this.asymmetricPredictor.predict(sentence)));
    }

    // Create the symmetric model
    private Criteria<String, float[]> createSymmetricCriteria() {
        return Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/multi-qa-MiniLM-L6-cos-v1")
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();
    }

    // Create the criteria for the asymmetric model
    private Criteria<String, float[]> createAsymmetricCriteria() {
        return Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/msmarco-distilbert-base-v4")
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();
    }

    // Close the models
    public void close() {
        if (this.asymmetricPredictor != null) this.asymmetricPredictor.close();
        if (this.symmetricModel != null) this.symmetricModel.close();
        System.out.println("Models closed.");
    }

    private static float[] normalizeVector(float[] in) {
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

    private List<Float> floatArrayToFlaotList(float[] in) {
        List<Float> out = new ArrayList<>();
        for (float f : in) {
            out.add(f);
        }
        return out;
    }
    public List<Float> generateSymmetricEmbedding(String query) {
        final int expectedDimension = 384;
        try {
            List<Float> vector = symmetricEmbed(query);
            if(vector.size() != expectedDimension){
                throw new IllegalArgumentException("Invalid vector dimension. Expected " + expectedDimension + " elements, but got " + vector.size() + " elements.");
            }
            return vector;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public List<Float> generateAsymmetricEmbedding(String query) {
        final int expectedDimension = 768;
        try {
            List<Float> vector = asymmetricEmbed(query);
            if(vector.size() != expectedDimension){
                throw new IllegalArgumentException("Invalid vector dimension. Expected " + expectedDimension + " elements, but got " + vector.size() + " elements.");
            }
            return vector;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }





}
