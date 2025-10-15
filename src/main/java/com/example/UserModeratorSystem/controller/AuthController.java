package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.dto.LoginDTO;
import com.example.UserModeratorSystem.dto.RegisterDTO;
import com.example.UserModeratorSystem.dto.LoginResponseDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    //Register API
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Void>> register(@Valid @RequestBody RegisterDTO userDto) {
        User user = userService.register(userDto);
        // Use constructor without data
        ApiResponseDTO<Void> apiResponseDTO = new ApiResponseDTO<>(true, "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDTO);
    }


    // Login API
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginDTO userDto) {
        LoginResponseDTO loginResponseDTO = userService.login(userDto);
        ApiResponseDTO<LoginResponseDTO> apiResponseDTO = new ApiResponseDTO<>(true, "Login successful", loginResponseDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }
}
