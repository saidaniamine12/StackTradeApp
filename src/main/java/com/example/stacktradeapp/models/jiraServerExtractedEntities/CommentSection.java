package com.example.stacktradeapp.models.jiraServerExtractedEntities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class CommentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxResults;

    private Integer total;

    private Integer startAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Comments> comments;

}
