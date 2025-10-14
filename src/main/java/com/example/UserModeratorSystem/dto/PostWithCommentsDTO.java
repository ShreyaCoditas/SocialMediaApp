package com.example.UserModeratorSystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostWithCommentsDTO {
    private Long id;
    private String username;
    private Long userId;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDTO> comments;
}
