package com.example.stacktradeapp.mongodb.services;


import com.example.stacktradeapp.exception.NotFoundException;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class MongoJiraTicketServiceImpl implements MongoJiraTicketService{
    private final Logger logger = LoggerFactory.getLogger(MongoJiraTicketServiceImpl.class);

    public final MongoTemplate mongoTemplate;



    public MongoJiraTicketServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public BasicDBObject getTicketById(String id) throws NotFoundException {
        Query query = new BasicQuery(String.valueOf(new BasicDBObject("id", id)));

        return mongoTemplate.findOne(query, BasicDBObject.class, "Spring");
    }
    public List<BasicDBObject> getSortedTicketsByIds(List<String> ids){

        List<BasicDBObject> basicDBObjectList = getTicketsByIds(ids);
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

    @Override
    public Collection<Document> insertTickets(List<Document> tickets) {
        Collection<Document> inserted =  mongoTemplate.insert(tickets, "Spring");
        logger.info("inserted" + tickets.size() + "to MongoDB");
        return inserted;
    }

    @Override
    public List<BasicDBObject> getTicketsByIds(List<String> ids) {
        logger.info("gettingTicketsByIdsFromMongoDB: " + ids);
        // Create a query to find documents with IDs in the list
        Query query = new Query(Criteria.where("id").in(ids));
        return mongoTemplate.find(query, BasicDBObject.class, "Spring");
    }


}
