package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.dto.UserDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.security.UserPrincipal;
import com.example.UserModeratorSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserDTO profile = userService.getUserProfile(userPrincipal.getUser());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "User profile retrieved", profile));
    }

    // Get all regular users
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "All users retrieved successfully", users));
    }

    // Get all moderators
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/moderators")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAllModerators() {
        List<UserDTO> moderators = userService.getAllModerators();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "All moderators retrieved successfully", moderators));
    }

}

