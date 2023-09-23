package com.example.stacktradeapp.repositories;

import com.example.stacktradeapp.models.ConfirmationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE confirmation_token c " +
            "SET c.confirmed_at = :confirmedAt " +
            "WHERE c.id = :id",nativeQuery = true)
    int updateConfirmedAt(Long id,
                          LocalDateTime confirmedAt);

    @Query(value = "SELECT * from confirmation_token WHERE user_id = :userId AND revoked = false",nativeQuery = true)
    List<ConfirmationToken> findAllValidTokenByUser(Integer userId);


    @Query(value = "SELECT * from confirmation_token WHERE token = :tokenString AND revoked = false",nativeQuery = true)
    Optional<ConfirmationToken> findByConfirmationToken(String tokenString);
}
