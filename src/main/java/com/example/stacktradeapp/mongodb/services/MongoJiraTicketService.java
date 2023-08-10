package com.example.stacktradeapp.mongodb.services;

import com.example.stacktradeapp.exception.NotFoundException;
import com.mongodb.BasicDBObject;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public interface MongoJiraTicketService {

    BasicDBObject getTicketById(String id) throws NotFoundException;

    List<BasicDBObject> getTicketsByIds(List<String> ids);

    void insertTickets(List<JSONObject> tickets);
}
