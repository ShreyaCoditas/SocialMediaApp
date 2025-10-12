package com.example.UserModeratorSystem.repository;

import com.example.UserModeratorSystem.entity.Role;
import com.example.UserModeratorSystem.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);
}

