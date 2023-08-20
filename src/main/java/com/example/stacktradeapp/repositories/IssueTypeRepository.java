package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueTypeRepository extends JpaRepository<IssueType, String> {
}
