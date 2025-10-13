package com.example.UserModeratorSystem.exception;

import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


     // Handles validation errors from @Valid annotated DTOs.

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponseDTO<Map<String, String>> response =
                new ApiResponseDTO<>(false, "Validation failed", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponseDTO<Void> response = new ApiResponseDTO<>(false, "Access Denied");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> handleUsernameExists(UserAlreadyExistsException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("username", ex.getMessage());
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.CONFLICT.value());

        ApiResponseDTO<Map<String, Object>> response =
                new ApiResponseDTO<>(false, "User registration failed", errors);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

   @ExceptionHandler(EmailNotFoundException.class)
   public ResponseEntity<ApiResponseDTO<Map<String,Object>>> handleEmailNotFound(EmailNotFoundException ex){
        Map<String,Object> errors=new HashMap<>();
        errors.put("email",ex.getMessage());
        errors.put("timestamp",LocalDateTime.now());
        errors.put("status",HttpStatus.NOT_FOUND.value());

        ApiResponseDTO<Map<String,Object>> response=new ApiResponseDTO<>(false,"email not found",errors);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
   }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponseDTO<Map<String,Object>>> handleInvalidPassword(InvalidPasswordException ex){
        Map<String,Object> errors=new HashMap<>();
        errors.put("password",ex.getMessage());
        errors.put("timestamp",LocalDateTime.now());
        errors.put("status",HttpStatus.BAD_REQUEST.value());

        ApiResponseDTO<Map<String,Object>> response=new ApiResponseDTO<>(false,"InValid Password",errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDTO<Map<String,Object>>> handleEmailAlreadyExists(EmailAlreadyExistsException ex){
        Map<String,Object> errors=new HashMap<>();
        errors.put("email",ex.getMessage());
        errors.put("timestamp",LocalDateTime.now());
        errors.put("status",HttpStatus.BAD_REQUEST.value());

        ApiResponseDTO<Map<String,Object>> response=new ApiResponseDTO<>(false,"email already exists",errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Map<String,Object>>>handlePostExceptions(PostNotFoundException ex){
        Map<String,Object> errors=new HashMap<>();
        errors.put("post",ex.getMessage());
        errors.put("timestamp",LocalDateTime.now());
        errors.put("status",HttpStatus.NOT_FOUND.value());

        ApiResponseDTO<Map<String,Object>> response=new ApiResponseDTO<>(false,"post not found",errors);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Map<String,Object>>> handleCommentExceptions(CommentNotFoundException ex) {
        Map<String,Object> errors=new HashMap<>();
        errors.put("comment",ex.getMessage());
        errors.put("timestamp",LocalDateTime.now());
        errors.put("status",HttpStatus.NOT_FOUND.value());
        ApiResponseDTO<Map<String,Object>> response= new ApiResponseDTO<>(false,"comment not found",errors);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


     // Handles known runtime exceptions from service/business logic (like "User not found").
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Map<String,Object>>> handleRuntimeExceptions(RuntimeException ex) {
        Map<String,Object> errors=new HashMap<>();
        errors.put("error",ex.getMessage());
        errors.put("timestamp",LocalDateTime.now());
        errors.put("status",HttpStatus.BAD_REQUEST.value());
        ApiResponseDTO<Map<String,Object>> response = new ApiResponseDTO<>(false, ex.getMessage(),errors);

        // For user-related/business logic errors, return 400 (not 500)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //  Handles all unexpected exceptions that aren't caught elsewhere.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<String>> handleAllExceptions(Exception ex) {
        ApiResponseDTO<String> response =
                new ApiResponseDTO<>(false, "Something went wrong: " + ex.getMessage());

        // Internal server error for unknown exceptions
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
