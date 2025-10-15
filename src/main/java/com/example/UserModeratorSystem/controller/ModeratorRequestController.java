package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.dto.ModeratorRequestDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.security.UserPrincipal;
import com.example.UserModeratorSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderator/request-access")
public class ModeratorRequestController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ModeratorRequestDTO>> requestModerator(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        ApiResponseDTO<ModeratorRequestDTO> response = userService.createModeratorRequest(user);
        return ResponseEntity.ok(response);
    }
}
