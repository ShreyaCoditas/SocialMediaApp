package com.example.UserModeratorSystem.exception;

public class InvalidPostStateException extends RuntimeException {
    public InvalidPostStateException(String message) {
        super(message);
    }
}
