package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.Fields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldsRepository extends JpaRepository<Fields, String> {
}
