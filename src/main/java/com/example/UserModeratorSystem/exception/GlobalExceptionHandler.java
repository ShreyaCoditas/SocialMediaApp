package com.example.UserModeratorSystem.exception;

import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


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
    public ResponseEntity<ApiResponseDTO<String>> handleUserExists(UserAlreadyExistsException ex) {
        ApiResponseDTO<String> response = new ApiResponseDTO<>(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<String>>handlePostExceptions(PostNotFoundException ex){
        ApiResponseDTO<String> response=new ApiResponseDTO<>(false,ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<String >> handleCommentExceptions(CommentNotFoundException e)
    {
        ApiResponseDTO<String> response= new ApiResponseDTO<>(false,e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


     // Handles known runtime exceptions from service/business logic (like "User not found").
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleRuntimeExceptions(RuntimeException ex) {
        ApiResponseDTO<String> response =
                new ApiResponseDTO<>(false, ex.getMessage());

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
