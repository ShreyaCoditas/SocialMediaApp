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
import com.example.UserModeratorSystem.exception.CommentNotFoundException;
import com.example.UserModeratorSystem.exception.PostNotFoundException;
import com.example.UserModeratorSystem.exception.UnauthorizedActionException;
import com.example.UserModeratorSystem.repository.CommentRepository;
import com.example.UserModeratorSystem.repository.PostRepository;
import com.example.UserModeratorSystem.repository.ReviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Comment comment = new Comment();

        Post post = postRepository.findById(createCommentDTO.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (post.getStatus() != Status.APPROVED) {
            throw new RuntimeException("Cannot comment on a disapproved/pending post");
        }

        comment.setPost(post);
        comment.setContent(createCommentDTO.getContent());
        comment.setUser(user);

        if ("SUPER_ADMIN".equals(user.getRole().getName().name())) {
            comment.setStatus(Status.APPROVED);
        } else {
            comment.setStatus(Status.PENDING);
        }

        comment = commentRepository.save(comment);
        CommentDTO dto = objectMapper.convertValue(comment, CommentDTO.class);
        dto.setUserId(comment.getUser().getId());
        dto.setPostId(comment.getPost().getId());
        dto.setUsername(comment.getUser().getUsername());
        return dto;
    }

    // Get all approved comments
//    public List<CommentDTO> getAllApprovedComments() {
//        return commentRepository.findAll().stream()
//                .filter(c -> c.getStatus() == Status.APPROVED)
//                .map(c -> {
//                    CommentDTO dto = objectMapper.convertValue(c, CommentDTO.class);
//                    dto.setUserId(c.getUser().getId());
//                    dto.setPostId(c.getPost().getId());
//                    dto.setUsername(c.getUser().getUsername());
//                    return dto;
//                })
//                .collect(Collectors.toList());
//    }

    // Get comment by ID
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        CommentDTO dto = objectMapper.convertValue(comment, CommentDTO.class);
        dto.setUserId(comment.getUser().getId());
        dto.setPostId(comment.getPost().getId());
        dto.setUsername(comment.getUser().getUsername());
        return dto;
    }

    // Get all comments by a user
    public List<CommentDTO> getCommentsByUser(Long userId) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(userId))
                .map(c -> {
                    CommentDTO dto = objectMapper.convertValue(c, CommentDTO.class);
                    dto.setUserId(c.getUser().getId());
                    dto.setPostId(c.getPost().getId());
                    dto.setUsername(c.getUser().getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Get comments by user and status
    public List<CommentDTO> getCommentsByUserAndStatus(Long userId, Status status) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(userId) && c.getStatus() == status)
                .map(c -> {
                    CommentDTO dto = objectMapper.convertValue(c, CommentDTO.class);
                    dto.setUserId(c.getUser().getId());
                    dto.setPostId(c.getPost().getId());
                    dto.setUsername(c.getUser().getUsername());
                    return dto;
                })
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
            throw new RuntimeException("Disapproved comments cannot be edited");
        }

        comment.setContent(updatedCommentDTO.getContent());
        comment.setStatus(Status.PENDING);

        comment = commentRepository.save(comment);

        CommentDTO dto = objectMapper.convertValue(comment, CommentDTO.class);
        dto.setUserId(comment.getUser().getId());
        dto.setPostId(comment.getPost().getId());
        dto.setUsername(comment.getUser().getUsername());
        return dto;
    }





    // Delete comment
    public String deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        String role = user.getRole().getName().name();

        // If not SUPER_ADMIN, ensure user owns the comment
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
                .map(comment -> {
                    CommentDTO dto = new CommentDTO();
                    dto.setId(comment.getId());
                    dto.setContent(comment.getContent());
                    dto.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
                    dto.setUserId(comment.getUser().getId());
                    dto.setUsername(comment.getUser().getUsername());
                    dto.setStatus(comment.getStatus().name());
                    dto.setCreatedAt(comment.getCreatedAt());
                    dto.setUpdatedAt(comment.getUpdatedAt());
                    return dto;
                })
                .toList();
    }


//    //  Delete comment By Admin
//    public String deleteCommentByAdmin(Long commentId) {
//        Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
//        commentRepository.delete(comment);
//        return "Comment deleted successfully";
//    }

    // Review a comment
    public Map<String, Object> reviewComment(Long commentId, User reviewer, ReviewDTO reviewDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        ReviewAction action = ReviewAction.valueOf(reviewDTO.getAction());

        // Prevent self-review
        if (comment.getUser().getId().equals(reviewer.getId())) {
            throw new RuntimeException("You cannot review your own comment");
        }

        // Prevent duplicate review
        Optional<ReviewLog> existingLog = reviewLogRepository.findByUserAndEntityIdAndEntityType(
                reviewer, commentId, EntityType.COMMENT
        );
        if (existingLog.isPresent()) {
            throw new RuntimeException("You already reviewed this comment");
        }

        ReviewLog log = new ReviewLog();
        log.setEntityId(commentId);
        log.setEntityType(EntityType.COMMENT);
        log.setAction(action);
        log.setUser(reviewer);
        log.setReviewedAt(LocalDateTime.now());
        reviewLogRepository.save(log);

        // Update comment status
        Status updatedStatus = updateCommentStatus(commentId);

        // Check for conflict and build message dynamically
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


    //  helper method to update comment status:
    private Status updateCommentStatus(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        List<ReviewLog> reviews = reviewLogRepository.findByEntityIdAndEntityType(commentId, EntityType.COMMENT);

        boolean hasApproved = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.APPROVED);
        boolean hasRejected = reviews.stream().anyMatch(r -> r.getAction() == ReviewAction.REJECTED);

        // If any conflict, reject the comment
        if (hasApproved && hasRejected) {
            comment.setStatus(Status.REJECTED);
        } else if (hasApproved) {
            comment.setStatus(Status.APPROVED);
        } else if (hasRejected) {
            comment.setStatus(Status.REJECTED);
        } else {
            comment.setStatus(Status.PENDING); // No reviews yet
        }

        commentRepository.save(comment);
        return comment.getStatus();
    }

}






