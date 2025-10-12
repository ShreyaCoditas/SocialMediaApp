package com.example.UserModeratorSystem.service;


import com.example.UserModeratorSystem.constants.EntityType;
import com.example.UserModeratorSystem.constants.ReviewAction;
import com.example.UserModeratorSystem.dto.*;
import com.example.UserModeratorSystem.entity.Post;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.entity.ReviewLog;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.exception.PostNotFoundException;
import com.example.UserModeratorSystem.repository.PostRepository;
import com.example.UserModeratorSystem.repository.ReviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Create post
    public PostDTO createPost(PostCreateDto createPostDTO, User user) {
        Post post = new Post();
        post.setUser(user);
        post.setTitle(createPostDTO.getTitle());
        post.setContent(createPostDTO.getContent());

        // SuperAdmin auto-approve
        if ("SUPER_ADMIN".equals(user.getRole().getName().name())) {
            post.setStatus(Status.APPROVED);
        } else {
            post.setStatus(Status.PENDING);
        }

        post = postRepository.save(post);
        PostDTO postDTO = objectMapper.convertValue(post, PostDTO.class);
        postDTO.setUserId(post.getUser().getId()); // Set userId manually
        postDTO.setUsername(post.getUser().getUsername());//set username manually
        return postDTO;
    }

    //homepage-to get all approved posts and comments
    public List<PostWithCommentsDTO> getHomepagePosts() {
        return postRepository.findAll().stream()
                .filter(p -> p.getStatus() == Status.APPROVED)
                .map(post -> {
                    // Convert Post → PostDTO
                    PostDTO postDTO = new PostDTO();
                    postDTO.setId(post.getId());
                    postDTO.setTitle(post.getTitle());
                    postDTO.setContent(post.getContent());
                    postDTO.setUserId( post.getUser().getId());
                    postDTO.setUsername( post.getUser().getUsername());
                    postDTO.setStatus(post.getStatus().name());
                    postDTO.setCreatedAt(post.getCreatedAt());
                    postDTO.setUpdatedAt(post.getUpdatedAt());

                    // Convert comments → CommentDTO
                    List<CommentDTO> commentDTOs = new ArrayList<>();
                    if (post.getComments() != null) {
                        commentDTOs = post.getComments().stream()
                                .filter(c -> c.getStatus() == Status.APPROVED)
                                .map(c -> {
                                    CommentDTO dto = new CommentDTO();
                                    dto.setId(c.getId());
                                    dto.setContent(c.getContent());
                                    dto.setPostId(post.getId());
                                    dto.setUserId( c.getUser().getId());
                                    dto.setUsername( c.getUser().getUsername());
                                    dto.setStatus(c.getStatus().name());
                                    dto.setCreatedAt(c.getCreatedAt());
                                    dto.setUpdatedAt(c.getUpdatedAt());
                                    return dto;
                                })
                                .toList();
                    }

                    return new PostWithCommentsDTO(postDTO, commentDTOs);
                })
                .toList();
    }



    // Get post by ID
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setUserId(post.getUser().getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setStatus(post.getStatus().name());
        return postDTO;
    }


    // Edit post (only by owner)
    public PostDTO editPost(Long postId, PostCreateDto updatedPostDTO, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to edit this post");
        }

        if(post.getStatus()==Status.REJECTED){
            throw new RuntimeException("you cannot edit disapproved post");
        }

        post.setTitle(updatedPostDTO.getTitle());
        post.setContent(updatedPostDTO.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        post.setStatus(Status.PENDING); // Re-review required after edit

        post = postRepository.save(post);

        PostDTO postDTO = objectMapper.convertValue(post, PostDTO.class);
        postDTO.setUserId(post.getUser().getId());
        postDTO.setUsername(post.getUser().getUsername());
        return postDTO;
    }

    // Get all posts by a specific user
    public List<PostDTO> getPostsByUser(Long userId) {
        return postRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .map(p -> {
                    PostDTO dto = objectMapper.convertValue(p, PostDTO.class);
                    dto.setUserId(p.getUser().getId());
                    dto.setUsername(p.getUser().getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Get all posts by a specific user with a specific status
    public List<PostDTO> getPostsByUserAndStatus(Long userId, Status status) {
        return postRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(userId) && p.getStatus() == status)
                .map(p -> {
                    PostDTO dto = objectMapper.convertValue(p, PostDTO.class);
                    dto.setUserId(p.getUser().getId());
                    dto.setUsername(p.getUser().getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }



    // Delete post (handles both user and admin cases)
    public String deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        // If not SUPER_ADMIN, ensure user owns the post
        String role = user.getRole().getName().name();
        if (!role.equals("SUPER_ADMIN") && !post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
        return "Post deleted successfully";
    }


    // Get all  posts
    public List<PostDTO> getPostsByStatus(Status status) {
        return postRepository.findAll().stream()
                .filter(post -> status == null || post.getStatus() == status) // filter by status if provided
                .map(post -> {
                    PostDTO dto = new PostDTO();
                    dto.setId(post.getId());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setUserId(post.getUser().getId());
                    dto.setUsername(post.getUser().getUsername());
                    dto.setStatus(post.getStatus().name());
                    dto.setCreatedAt(post.getCreatedAt());
                    dto.setUpdatedAt(post.getUpdatedAt());
                    return dto;
                })
                .toList();
    }


//    //  Delete post
//    public String deletePostByAdmin(Long postId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new PostNotFoundException("Post not found"));
//        postRepository.delete(post);
//        return "Post deleted successfully";
//    }

    public Map<String, Object> reviewPost(Long postId, User reviewer, ReviewDTO reviewDTO) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        ReviewAction action = ReviewAction.valueOf(reviewDTO.getAction());

        if (post.getUser().getId().equals(reviewer.getId())) {
            throw new RuntimeException("You cannot review your own post");
        }

        Optional<ReviewLog> existingLog = reviewLogRepository.findByUserAndEntityIdAndEntityType(
                reviewer, postId, EntityType.POST
        );
        if (existingLog.isPresent()) {
            throw new RuntimeException("You already reviewed this post");
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

        //  helper method to update post status
    private Status updatePostStatus(Post post) {
        List<ReviewLog> reviews = reviewLogRepository.findByEntityIdAndEntityType(post.getId(), EntityType.POST);
        boolean hasApproved = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.APPROVED);
        boolean hasRejected = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.REJECTED);

        // If any conflict, reject the post
        if (hasApproved && hasRejected) {
            post.setStatus(Status.REJECTED);
        } else if (hasApproved) {
            post.setStatus(Status.APPROVED);
        } else if (hasRejected) {
            post.setStatus(Status.REJECTED);
        } else {
            post.setStatus(Status.PENDING); // no reviews yet
        }

        postRepository.save(post);
        return post.getStatus();
    }


}
