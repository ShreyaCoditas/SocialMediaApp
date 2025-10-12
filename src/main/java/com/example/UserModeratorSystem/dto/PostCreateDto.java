package com.example.UserModeratorSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateDto {

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Size(min = 3, message = "Content must be at least 3 characters long")
    private String content;
}

