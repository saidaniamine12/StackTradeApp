package com.example.stacktradeapp.mongodb.services;


import com.example.stacktradeapp.exception.NotFoundException;
import com.mongodb.BasicDBObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class MongoJiraTicketService {
    private static final Logger logger = LogManager.getLogger(MongoJiraTicketService.class);

    public final MongoTemplate mongoTemplate;

    public MongoJiraTicketService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public BasicDBObject getTicketById(String id) throws NotFoundException {
        Query query = new BasicQuery(String.valueOf(new BasicDBObject("id", id)));

        return mongoTemplate.findOne(query, BasicDBObject.class, "Spring");
    }
    public List<BasicDBObject> getTicketsByIds(List<String> ids){
        logger.info("gettingTicketsByIdsFromMongoDB: " + ids);

        // Create a query to find documents with IDs in the list
        Query query = new Query(Criteria.where("id").in(ids));

        List<BasicDBObject> basicDBObjectList = mongoTemplate.find(query, BasicDBObject.class, "Spring");

        Map<String, BasicDBObject> documentsMap = new LinkedHashMap<>();
        for (BasicDBObject document : basicDBObjectList) {
            String id = document.get("id").toString();
            documentsMap.put(id, document);
        }

        List<BasicDBObject> sortedList = new ArrayList<>();
        for (String id : ids) {
            BasicDBObject document = documentsMap.get(id);
            if (document != null) {
                sortedList.add(document);
            }
        }
        if (sortedList.size() != ids.size()) {
            logger.error("Some documents were not found in MongoDB");
        }

        return  sortedList;
    }

}
