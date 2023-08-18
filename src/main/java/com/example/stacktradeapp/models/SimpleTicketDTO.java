package com.example.stacktradeapp.models;

import com.example.stacktradeapp.exception.DocumentParsingException;
import com.mongodb.BasicDBObject;
import lombok.Data;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

@Data
public class SimpleTicketDTO {
    private String id;
    private String summary;
    private String projectName;
    private String description;
    private String resolutionDate;
    private String reporterName;
    private String assigneeName;
    private String key;


    // Constructors, getters, and setters
    //passing a BasicDBObject to the constructor to get the fields of the document
    //and assign them to the fields of the entity to return it to the frontend
    public SimpleTicketDTO(BasicDBObject document) throws DocumentParsingException {
        try {
            this.id = document.getString("_id");
            this.key = document.getString("key");
            //supposedly to be _id not id so if error occurs try to change it
            Document fields = (Document) document.get("fields");
            this.summary = fields.getString("summary") != null ? fields.getString("summary") : "";
            this.description = fields.getString("description") != null ? fields.getString("description") : "";
            this.resolutionDate = fields.getString("resolutiondate") != null ? fields.getString("resolutiondate") : "";
            Document project = (Document) fields.get("project");
            this.projectName = project.getString("name") != null ? project.getString("name") : "";
            Document reporter = (Document) fields.get("reporter");
            this.reporterName = reporter.getString("displayName") != null ? reporter.getString("displayName") : "";
            Document assignee =  fields.get("assignee") != null ? (Document) fields.get("assignee") : null ;
            if (assignee != null){
                this.assigneeName = assignee.getString("displayName") != null ? assignee.getString("displayName") : "" ;
            } else {
                this.assigneeName = "";
            }

        } catch (NullPointerException | ClassCastException e) {
            throw new DocumentParsingException("The document is not in the correct format" + e.getMessage());
        }

    }

    //passing a list of BasicDBObject to the constructor to get the fields of the documents
    public static List<SimpleTicketDTO> basicDocToTicketDTOMapper(List<BasicDBObject> documents) throws DocumentParsingException {
        List<SimpleTicketDTO> simpleTicketDTOList = new ArrayList<>();
        for (BasicDBObject document : documents) {
            SimpleTicketDTO simpleTicketDTO = new SimpleTicketDTO(document);
            simpleTicketDTOList.add(simpleTicketDTO);
        }
        return simpleTicketDTOList;
    }

}
