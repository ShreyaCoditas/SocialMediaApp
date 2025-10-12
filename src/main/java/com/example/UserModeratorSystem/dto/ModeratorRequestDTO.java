package com.example.UserModeratorSystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorRequestDTO {
    private Long id;
    private String username;
    private Long userId;
    private String status; // PENDING, APPROVED, REJECTED
//    @JsonIgnore
    private Long reviewedBy;
}
