package com.example.UserModeratorSystem.repository;

import com.example.UserModeratorSystem.entity.ModeratorRequest;
import com.example.UserModeratorSystem.constants.RequestStatus;
import com.example.UserModeratorSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeratorRequestRepository extends JpaRepository<ModeratorRequest, Long> {

    // method to find a request by user + exact status
    Optional<ModeratorRequest> findByUserAndStatus(User user, RequestStatus status);

}
