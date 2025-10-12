package com.example.UserModeratorSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long entityId;       // Post or Comment ID
    private String entityType;   // "POST" or "COMMENT"
    private String action; // APPROVED, REJECTED
}

