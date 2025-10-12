package com.example.UserModeratorSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {

    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotBlank(message = "Comment content cannot be blank")
    @Size(min = 2, max = 500, message = "Comment must be between 2 and 500 characters")
    private String content;
}
