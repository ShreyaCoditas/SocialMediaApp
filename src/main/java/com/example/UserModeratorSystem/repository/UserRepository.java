package com.example.UserModeratorSystem.repository;

import com.example.UserModeratorSystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.UserModeratorSystem.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRole(Role userRole);
}
