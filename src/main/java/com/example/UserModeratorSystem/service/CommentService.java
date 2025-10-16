package com.example.UserModeratorSystem.service;

import com.example.UserModeratorSystem.constants.EntityType;
import com.example.UserModeratorSystem.constants.ReviewAction;
import com.example.UserModeratorSystem.dto.CommentDTO;
import com.example.UserModeratorSystem.dto.CreateCommentDTO;
import com.example.UserModeratorSystem.dto.ReviewDTO;
import com.example.UserModeratorSystem.entity.Comment;
import com.example.UserModeratorSystem.entity.Post;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.entity.ReviewLog;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.exception.*;
import com.example.UserModeratorSystem.repository.CommentRepository;
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
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReviewLogRepository reviewLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Create comment
    public CommentDTO createComment(CreateCommentDTO createCommentDTO, User user) {
        Post post = postRepository.findById(createCommentDTO.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (post.getStatus() != Status.APPROVED) {
            throw new InvalidPostStateException("Cannot comment on a disapproved/pending post");
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(createCommentDTO.getContent());
        comment.setUser(user);

        comment.setStatus("SUPER_ADMIN".equals(user.getRole().getName().name()) ? Status.APPROVED : Status.PENDING);

        comment = commentRepository.save(comment);
        return mapToCommentDTO(comment);
    }

    // Get comment by ID
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        return mapToCommentDTO(comment);
    }

    // Get all comments by a user
    public List<CommentDTO> getCommentsByUser(Long userId) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(userId))
                .map(this::mapToCommentDTO)
                .collect(Collectors.toList());
    }

    // Get comments by user and status
    public List<CommentDTO> getCommentsByUserAndStatus(Long userId, Status status) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(userId) && c.getStatus() == status)
                .map(this::mapToCommentDTO)
                .collect(Collectors.toList());
    }

    // Edit comment
    public CommentDTO editComment(Long commentId, CreateCommentDTO updatedCommentDTO, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You are not authorized to edit this comment");
        }

        if (comment.getStatus() == Status.REJECTED) {
            throw new CustomException("Disapproved comments cannot be edited", HttpStatus.FORBIDDEN);
        }

        comment.setContent(updatedCommentDTO.getContent());
        comment.setStatus(Status.PENDING);
        comment = commentRepository.save(comment);

        return mapToCommentDTO(comment);
    }

    // Delete comment
    public String deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        String role = user.getRole().getName().name();
        if (!role.equals("SUPER_ADMIN") && !comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
        return "Comment deleted successfully";
    }

    // Get all comments by status
    public List<CommentDTO> getCommentsByStatus(Status status) {
        return commentRepository.findAll().stream()
                .filter(comment -> status == null || comment.getStatus() == status)
                .map(this::mapToCommentDTO)
                .collect(Collectors.toList());
    }

    // Review a comment
    public Map<String, Object> reviewComment(Long commentId, User reviewer, ReviewDTO reviewDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        ReviewAction action = ReviewAction.valueOf(reviewDTO.getAction());

        if (comment.getUser().getId().equals(reviewer.getId())) {
            throw new CustomException("You cannot review your own comment", HttpStatus.FORBIDDEN);
        }

        Optional<ReviewLog> existingLog = reviewLogRepository.findByUserAndEntityIdAndEntityType(
                reviewer, commentId, EntityType.COMMENT
        );
        if (existingLog.isPresent()) {
            throw new CustomException("You already reviewed this comment", HttpStatus.FORBIDDEN);
        }

        ReviewLog log = new ReviewLog();
        log.setEntityId(commentId);
        log.setEntityType(EntityType.COMMENT);
        log.setAction(action);
        log.setUser(reviewer);
        log.setReviewedAt(LocalDateTime.now());
        reviewLogRepository.save(log);

        Status updatedStatus = updateCommentStatus(commentId);

        boolean hasApproved = reviewLogRepository.findByEntityIdAndEntityType(commentId, EntityType.COMMENT)
                .stream().anyMatch(r -> r.getAction() == ReviewAction.APPROVED);
        boolean hasRejected = reviewLogRepository.findByEntityIdAndEntityType(commentId, EntityType.COMMENT)
                .stream().anyMatch(r -> r.getAction() == ReviewAction.REJECTED);

        String message;
        if (hasApproved && hasRejected) {
            message = "Comment reviewed but due to conflict rejected";
        } else if (updatedStatus == Status.APPROVED) {
            message = "Comment reviewed and approved";
        } else {
            message = "Comment reviewed and rejected";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("reviewedById", reviewer.getId());
        return response;
    }

    // Helper method to update comment status
    private Status updateCommentStatus(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        List<ReviewLog> reviews = reviewLogRepository.findByEntityIdAndEntityType(commentId, EntityType.COMMENT);

        boolean hasApproved = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.APPROVED);
        boolean hasRejected = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.REJECTED);

        if (hasApproved && hasRejected) {
            comment.setStatus(Status.REJECTED);
        } else if (hasApproved) {
            comment.setStatus(Status.APPROVED);
        } else if (hasRejected) {
            comment.setStatus(Status.REJECTED);
        }

        commentRepository.save(comment);
        return comment.getStatus();
    }

    // Helper method: map Comment entity to CommentDTO
    private CommentDTO mapToCommentDTO(Comment comment) {
        CommentDTO dto = objectMapper.convertValue(comment, CommentDTO.class);
        dto.setUserId(comment.getUser().getId());
        dto.setPostId(comment.getPost().getId());
        dto.setUsername(comment.getUser().getUsername());
        return dto;
    }
}




