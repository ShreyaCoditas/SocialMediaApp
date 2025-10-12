package com.example.UserModeratorSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostWithCommentsDTO {
    private PostDTO post;
    private List<CommentDTO> comments;
}
