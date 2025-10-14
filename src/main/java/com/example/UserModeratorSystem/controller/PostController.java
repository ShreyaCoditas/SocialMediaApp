package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.dto.PostCreateDto;
import com.example.UserModeratorSystem.dto.PostDTO;
//import com.example.UserModeratorSystem.dto.UserPostsByStatusDTO;
import com.example.UserModeratorSystem.dto.PostWithCommentsDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.security.UserPrincipal;
import com.example.UserModeratorSystem.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @PostMapping("/publish")
    public ResponseEntity<ApiResponseDTO<PostDTO>> createPost(
          @Valid @RequestBody PostCreateDto createPostDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userPrincipal.getUser();
        PostDTO createdPost = postService.createPost(createPostDTO, user);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Post created", createdPost));
    }


    // homepage
//    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
//    @GetMapping
//    public ResponseEntity<ApiResponseDTO<List<PostWithCommentsDTO>>> getHomepagePosts() {
//        List<PostWithCommentsDTO> postswithcomments = postService.getHomepagePosts();
//        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Home Page", postswithcomments));
//    }

//    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
//    @GetMapping
//    public ResponseEntity<ApiResponseDTO<List<PostDTO>>> getHomepagePosts() {
//        List<PostDTO> posts = postService.getHomepagePosts();
//        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Home Page", posts));
//    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PostWithCommentsDTO>>> getHomepagePosts() {
        List<PostWithCommentsDTO> posts = postService.getHomepagePosts();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Home Page", posts));
    }



    // Get post by ID
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PostDTO>> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Post retrieved", post));
    }

    //  Edit own post
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponseDTO<PostDTO>> editPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostCreateDto updatedPostDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userPrincipal.getUser();
        PostDTO updatedPost = postService.editPost(postId, updatedPostDTO, user);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Post updated successfully", updatedPost));
    }

    // Get all posts by a specific user
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<PostDTO>>> getPostsByUser(@PathVariable Long userId) {
        List<PostDTO> posts = postService.getPostsByUser(userId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "User posts retrieved", posts));
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN', 'USER')")
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDTO<List<PostDTO>>> getPostsByStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("status") Status status) {
        User user = userPrincipal.getUser();
        List<PostDTO> posts = postService.getPostsByUserAndStatus(user.getId(), status);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Posts retrieved by status", posts));
    }



    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'SUPER_ADMIN')")
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<ApiResponseDTO<String>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userPrincipal.getUser();
        String message = postService.deletePost(postId, user);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, message));
    }

}
