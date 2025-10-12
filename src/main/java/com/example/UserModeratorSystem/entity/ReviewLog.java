package com.example.UserModeratorSystem.entity;


import com.example.UserModeratorSystem.constants.EntityType;
import com.example.UserModeratorSystem.constants.ReviewAction;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "review_logs")
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // reviewer

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ReviewAction action;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt = LocalDateTime.now();

    public ReviewLog(User user, EntityType entityType, Long entityId, ReviewAction action, LocalDateTime reviewedAt) {
        this.user = user;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.reviewedAt = reviewedAt;
    }
}
