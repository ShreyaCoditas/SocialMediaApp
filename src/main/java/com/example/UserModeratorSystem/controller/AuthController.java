package com.example.UserModeratorSystem.controller;


import com.example.UserModeratorSystem.dto.LoginDTO;
import com.example.UserModeratorSystem.dto.RegisterDTO;
import com.example.UserModeratorSystem.dto.ResponseDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    // Register API
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<ResponseDTO>> register(@Valid @RequestBody RegisterDTO userDto) {
        User user = service.register(userDto);
        ResponseDTO responseDTO = new ResponseDTO("Registration successful for " + user.getUsername());
        ApiResponseDTO<ResponseDTO> apiResponseDTO = new ApiResponseDTO<>(true, "User registered successfully",responseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDTO);
    }

//    @PostMapping("/register")
//    public ResponseEntity<ApiResponseDTO<Void>> register(@Valid @RequestBody RegisterDTO userDto) {
//        User user = service.register(userDto);
//        // Use constructor without data
//        ApiResponseDTO<Void> apiResponseDTO = new ApiResponseDTO<>(true, "User registered successfully");
//        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDTO);
//    }


    // Login API
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<ResponseDTO>> login(@Valid @RequestBody LoginDTO userDto) {
        ResponseDTO responseDTO = service.verify(userDto);
        ApiResponseDTO<ResponseDTO> apiResponseDTO = new ApiResponseDTO<>(true, "Login successful", responseDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }
}
