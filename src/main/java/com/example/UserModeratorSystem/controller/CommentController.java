package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.dto.CommentDTO;
import com.example.UserModeratorSystem.dto.CreateCommentDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.security.UserPrincipal;
import com.example.UserModeratorSystem.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // Create comment

    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @PostMapping("/publish")
    public ResponseEntity<ApiResponseDTO<CommentDTO>> createComment(
           @Valid @RequestBody CreateCommentDTO createCommentDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userPrincipal.getUser();
        CommentDTO createdComment = commentService.createComment(createCommentDTO, user);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comment created", createdComment));
    }

    // Get comment by ID

    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CommentDTO>> getCommentById(@PathVariable Long id) {
        CommentDTO comment = commentService.getCommentById(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comment retrieved", comment));
    }

    // Get all comments by a specific user
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<CommentDTO>>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentDTO> comments = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "User comments retrieved", comments));
    }

    // Edit comment (only own comment)
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CommentDTO>> editComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentDTO updatedCommentDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userPrincipal.getUser();
        CommentDTO editedComment = commentService.editComment(id, updatedCommentDTO, user);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comment updated", editedComment));
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDTO<List<CommentDTO>>> getCommentsByStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("status") Status status) {
        User user = userPrincipal.getUser();
        List<CommentDTO> comments = commentService.getCommentsByUserAndStatus(user.getId(), status);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comments retrieved by status", comments));
    }

    // Delete comment (only own comment)
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDTO<String>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userPrincipal.getUser();
        commentService.deleteComment(id, user);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comment deleted"));
    }



}
