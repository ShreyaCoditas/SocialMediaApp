package com.example.UserModeratorSystem.service;

import com.example.UserModeratorSystem.constants.EntityType;
import com.example.UserModeratorSystem.constants.ReviewAction;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.dto.*;
import com.example.UserModeratorSystem.entity.Post;
import com.example.UserModeratorSystem.entity.ReviewLog;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.exception.CustomException;
import com.example.UserModeratorSystem.exception.PostNotFoundException;
import com.example.UserModeratorSystem.exception.UnauthorizedActionException;
import com.example.UserModeratorSystem.repository.PostRepository;
import com.example.UserModeratorSystem.repository.ReviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReviewLogRepository reviewLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    //To create a Post
    public PostDTO createPost(PostCreateDTO createPostDTO, User user) {
        Post post = new Post();
        post.setUser(user);
        post.setTitle(createPostDTO.getTitle());
        post.setContent(createPostDTO.getContent());
        //superadmin post autoapproved,else pending
        post.setStatus("SUPER_ADMIN".equals(user.getRole().getName().name())
                ? Status.APPROVED
                : Status.PENDING);

        post = postRepository.save(post);
        return mapToPostDTO(post);
    }

    //Retrieve all approved posts and comments for homepage display.
    public List<PostWithCommentsDTO> getHomepagePosts() {
        return postRepository.findAll().stream()
                .filter(post -> post.getStatus() == Status.APPROVED)
                .map(post -> {
                    PostWithCommentsDTO postDTO = new PostWithCommentsDTO();
                    postDTO.setId(post.getId());
                    postDTO.setUsername(post.getUser().getUsername());
                    postDTO.setUserId(post.getUser().getId());
                    postDTO.setTitle(post.getTitle());
                    postDTO.setContent(post.getContent());
                    postDTO.setStatus(post.getStatus().name());
                    postDTO.setCreatedAt(post.getCreatedAt());
                    postDTO.setUpdatedAt(post.getUpdatedAt());

                    List<CommentDTO> commentDTOs = post.getComments().stream()
                            .map(comment -> {
                                CommentDTO dto = new CommentDTO();
                                dto.setId(comment.getId());
                                dto.setUsername(comment.getUser().getUsername());
                                dto.setPostId(post.getId());
                                dto.setUserId(comment.getUser().getId());
                                dto.setContent(comment.getContent());
                                dto.setStatus(comment.getStatus().name());
                                dto.setCreatedAt(comment.getCreatedAt());
                                dto.setUpdatedAt(comment.getUpdatedAt());
                                return dto;
                            })
                            .toList();

                    postDTO.setComments(commentDTOs);
                    return postDTO;
                })
                .toList();
    }


    //Get a single post by ID.
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
        return mapToPostDTO(post);
    }

    //Edit post (allowed only for owner).
    public PostDTO editPost(Long postId, PostCreateDTO updatedPostDTO, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You are not authorized to edit this post");
        }

        if (post.getStatus() == Status.REJECTED) {
            throw new CustomException("You cannot edit a disapproved post", HttpStatus.FORBIDDEN);
        }

        post.setTitle(updatedPostDTO.getTitle());
        post.setContent(updatedPostDTO.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        post.setStatus(Status.PENDING); // Requires re-review after editing

        post = postRepository.save(post);
        return mapToPostDTO(post);
    }


    //Get all posts by a specific user.
    public List<PostDTO> getPostsByUser(Long userId) {
        return postRepository.findAll().stream()
                .filter(post -> post.getUser().getId().equals(userId))
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    //Get all posts by a user filtered by status.
    public List<PostDTO> getPostsByUserAndStatus(Long userId, Status status) {
        return postRepository.findAll().stream()
                .filter(post -> post.getUser().getId().equals(userId) && post.getStatus() == status)
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    //Delete post (Super Admin can delete any,others can delete only their own).
    public String deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        String role = user.getRole().getName().name();
        if (!"SUPER_ADMIN".equals(role) && !post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
        return "Post deleted successfully";
    }

    //Get all posts by status.
    public List<PostDTO> getPostsByStatus(Status status) {
        return postRepository.findAll().stream()
                .filter(post -> status == null || post.getStatus() == status)
                .map(this::mapToPostDTO)
                .toList();
    }

    //Review post by moderator or super admin.
    public Map<String, Object> reviewPost(Long postId, User reviewer, ReviewDTO reviewDTO) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        ReviewAction action = ReviewAction.valueOf(reviewDTO.getAction());

        if (post.getUser().getId().equals(reviewer.getId())) {
            throw new CustomException("You cannot review your own post", HttpStatus.FORBIDDEN);
        }

        Optional<ReviewLog> existingLog = reviewLogRepository.findByUserAndEntityIdAndEntityType(
                reviewer, postId, EntityType.POST
        );
        if (existingLog.isPresent()) {
            throw new CustomException("You already reviewed this post", HttpStatus.FORBIDDEN);
        }

        ReviewLog log = new ReviewLog();
        log.setEntityId(postId);
        log.setEntityType(EntityType.POST);
        log.setAction(action);
        log.setUser(reviewer);
        log.setReviewedAt(LocalDateTime.now());
        reviewLogRepository.save(log);

        Status updatedStatus = updatePostStatus(post);

        boolean hasApproved = reviewLogRepository.findByEntityIdAndEntityType(postId, EntityType.POST)
                .stream().anyMatch(r -> r.getAction() == ReviewAction.APPROVED);
        boolean hasRejected = reviewLogRepository.findByEntityIdAndEntityType(postId, EntityType.POST)
                .stream().anyMatch(r -> r.getAction() == ReviewAction.REJECTED);

        String message;
        if (hasApproved && hasRejected) {
            message = "Post reviewed but due to conflict rejected";
        } else if (updatedStatus == Status.APPROVED) {
            message = "Post reviewed and approved";
        } else {
            message = "Post reviewed and rejected";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("reviewedById", reviewer.getId());
        return response;
    }

    private Status updatePostStatus(Post post) {
        List<ReviewLog> reviews = reviewLogRepository.findByEntityIdAndEntityType(post.getId(), EntityType.POST);
        boolean hasApproved = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.APPROVED);
        boolean hasRejected = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.REJECTED);

        if (hasApproved && hasRejected) {
            post.setStatus(Status.REJECTED);
        } else if (hasApproved) {
            post.setStatus(Status.APPROVED);
        } else if (hasRejected) {
            post.setStatus(Status.REJECTED);
        }

        postRepository.save(post);
        return post.getStatus();
    }

    private PostDTO mapToPostDTO(Post post) {
        PostDTO dto = objectMapper.convertValue(post, PostDTO.class);

        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setUsername(post.getUser().getUsername());
        }
        return dto;
    }
}

