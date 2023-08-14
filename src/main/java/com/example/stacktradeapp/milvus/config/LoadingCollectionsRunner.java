package com.example.stacktradeapp.milvus.config;

import com.example.stacktradeapp.milvus.vectorRepository.MilvusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LoadingCollectionsRunner implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(LoadingCollectionsRunner.class);

    private final MilvusRepository milvusRepository;

    public LoadingCollectionsRunner(MilvusRepository milvusRepository) {
        this.milvusRepository = milvusRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadCollectionWithRetries("spring_jira_summary_Collection");
        loadCollectionWithRetries("spring_jira_description_Collection");
    }

    private void loadCollectionWithRetries(String collectionName) {
        int maxRetries = 5;
        int retryIntervalSeconds = 30;

        for (int retry = 1; retry <= maxRetries; retry++) {
            boolean isLoaded = milvusRepository.loadCollectionToMemory(collectionName);
            if (isLoaded) {
                // Collection loaded successfully, exit the loop
                break;
            } else {
                if (retry < maxRetries) {
                    // Collection not loaded, wait and retry
                    try {
                        Thread.sleep(retryIntervalSeconds * 1000); // Convert to milliseconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        // Handle the exception
                    }
                } else {
                    // Max retries reached, log an error and shut down the app
                    logger.error("Collection '{}' is not loaded to memory after {} retries.", collectionName, maxRetries);
                }
            }
        }
    }
}
