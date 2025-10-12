package com.example.UserModeratorSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLogDTO {
    private Long id;
    private String userName;
    private Long userId;
    private String entityType;
    private Long entityId;
    private String action; // APPROVED, REJECTED, PENDING
    private String reviewedAt;
}

