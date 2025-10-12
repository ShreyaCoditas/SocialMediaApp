package com.example.UserModeratorSystem.entity;

import com.example.UserModeratorSystem.constants.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "moderator_requests")
public class ModeratorRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy; // super admin reviewer

    @Column(name = "requested_at")
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public ModeratorRequest(User user, RequestStatus status, User reviewedBy, LocalDateTime requestedAt, LocalDateTime reviewedAt) {
        this.user = user;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
    }
}
