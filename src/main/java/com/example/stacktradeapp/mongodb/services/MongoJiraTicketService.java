package com.example.stacktradeapp.mongodb.services;

import com.example.stacktradeapp.elasticsearch.services.impl.ElasticJiraTicketServiceImpl;
import com.example.stacktradeapp.exception.NotFoundException;
import com.mongodb.BasicDBObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


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
//    public List<BasicDBObject> searchTickets (String searchText) throws IOException {
//        Map<String, Double> idsWithScoreMap  = elasticJiraTicketService.textSearchQuery(searchText);
//        List<String> idsList = new ArrayList<>(idsWithScoreMap.keySet());
//
//        Query queryOrder = new Query(Criteria.where("id").in(idsList));
//
//        List<BasicDBObject> basicDBObjectList = mongoTemplate.find(queryOrder,BasicDBObject.class , "Spring");
//
//        List<BasicDBObject> sortedList = new ArrayList<>(basicDBObjectList.stream()
//                .sorted(Comparator.comparingDouble(obj -> idsWithScoreMap.getOrDefault(obj.getString("id"), 0.0)))
//                .toList());
//
//        logger.info("returned " + basicDBObjectList.size()+ " documents: ");
//        Collections.reverse(sortedList);
//        return  sortedList;
//    }

}
