package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JiraUserRepository extends JpaRepository<JiraUser, String> {
    Optional<JiraUser> findByKey(String key);
}
