package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, String> {
}
