package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.dto.*;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.security.UserPrincipal;
import com.example.UserModeratorSystem.service.CommentService;
import com.example.UserModeratorSystem.service.PostService;
import com.example.UserModeratorSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    // Get all  posts by status
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/posts")
    public ResponseEntity<ApiResponseDTO<List<PostDTO>>> getPostsByStatus(
            @RequestParam(value = "status", required = false) Status status) {

        List<PostDTO> posts = postService.getPostsByStatus(status);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Posts retrieved", posts));
    }

    // Get all  comments by status
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/comments")

    public ResponseEntity<ApiResponseDTO<List<CommentDTO>>> getCommentsByStatus(
            @RequestParam(name = "status", required = false) Status status) {

        List<CommentDTO> comments = commentService.getCommentsByStatus(status);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comments retrieved", comments));
    }


    // Review a post
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @PostMapping("/posts/{postId}/review")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> reviewPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ReviewDTO reviewDTO) {

        Map<String, Object> data = postService.reviewPost(postId, userPrincipal.getUser(), reviewDTO);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Post reviewed successfully", data));
    }

 //     Review a comment
     @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
     @PostMapping("/comments/{commentId}/review")

    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> reviewComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestBody ReviewDTO reviewDTO) {

    Map<String, Object> data = commentService.reviewComment(commentId, userPrincipal.getUser(), reviewDTO);
    return ResponseEntity.ok(new ApiResponseDTO<>(true, "Comment reviewed successfully", data));
}

//To Retire a Moderator
@PreAuthorize("hasRole('MODERATOR')")
@PutMapping("/retire")
public ResponseEntity<ApiResponseDTO<UserDTO>> retireAsModerator(
        @AuthenticationPrincipal UserPrincipal userPrincipal) {

    UserDTO updatedUser = userService.retireAsModerator(userPrincipal.getUser());
    return ResponseEntity.ok(new ApiResponseDTO<>(true, "You have successfully retired as a moderator. You are now a regular user.", updatedUser));
}
}
