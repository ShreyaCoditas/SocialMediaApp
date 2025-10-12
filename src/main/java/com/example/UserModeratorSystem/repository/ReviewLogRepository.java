package com.example.UserModeratorSystem.repository;

import com.example.UserModeratorSystem.constants.EntityType;
import com.example.UserModeratorSystem.entity.ReviewLog;
import com.example.UserModeratorSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    // Find all logs for a given entity (post/comment)
    List<ReviewLog> findByEntityIdAndEntityType(Long entityId, EntityType entityType);

    // Find if a specific user has already reviewed a specific entity
    Optional<ReviewLog> findByUserAndEntityIdAndEntityType(User user, Long entityId, EntityType entityType);
}

