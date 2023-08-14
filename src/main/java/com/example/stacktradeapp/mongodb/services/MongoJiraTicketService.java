package com.example.stacktradeapp.mongodb.services;

import com.example.stacktradeapp.exception.NotFoundException;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
@Service
public interface MongoJiraTicketService {

    BasicDBObject getTicketById(String id) throws NotFoundException;

    List<BasicDBObject> getSortedTicketsByIds(List<String> ids);


    Collection<Document> insertTickets(List<Document> tickets);

    List<BasicDBObject> getTicketsByIds(List<String> ids);
}
