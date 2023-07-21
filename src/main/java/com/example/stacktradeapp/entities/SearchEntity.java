package com.example.stacktradeapp.entities;

import com.mongodb.BasicDBObject;
import lombok.Data;
import org.bson.Document;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.ArrayList;
import java.util.List;

@Data
@EntityScan
public class SearchEntity {
    private String id;
    private String summary;
    private String projectName;
    private String description;
    private String created;
    private String creatorName;
    private String creatorEmailAddress;


    // Constructors, getters, and setters
    //passing a BasicDBObject to the constructor to get the fields of the document
    //and assign them to the fields of the entity to return it to the frontend
    public SearchEntity(BasicDBObject document) {
        this.id = document.getString("id");
        Document fields = (Document) document.get("fields");
        this.summary = fields.getString("summary");
        this.description = fields.getString("description");
        this.created = fields.getString("created");
        Document project = (Document) fields.get("project");
        this.projectName = project.getString("name");
        Document creator = (Document) fields.get("creator");
        this.creatorName = creator.getString("name");
        this.creatorEmailAddress = creator.getString("emailAddress");

    }

    //passing a list of BasicDBObject to the constructor to get the fields of the documents
    public static List<SearchEntity> basicDocToSearchEntity(List<BasicDBObject> documents) {
        List<SearchEntity> searchEntityList = new ArrayList<>();
        for (BasicDBObject document : documents) {
            SearchEntity searchEntity = new SearchEntity(document);
            searchEntityList.add(searchEntity);
        }
        return searchEntityList;
    }






}
