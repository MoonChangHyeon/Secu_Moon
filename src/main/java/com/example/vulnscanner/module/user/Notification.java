package com.example.vulnscanner.module.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    private String link;

    private boolean checked = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Optional: If we want to target specific users, add ManyToOne User.
    // For now, assuming these are system-wide admin notifications or we'll filter
    // by admin role in service.
    // Or we can add a 'targetUser' field if necessary.
    // User request implied "Admin Alarm", so maybe all admins see it.
    // But usually notifications are per-user.
    // Let's create a linkage to User if it's for a specific person, or handle
    // "Broadcast" logic later.
    // Given the context "Admin Alarm", it's likely for all admins.
    // Simple implementation: Store notification, all admins fetch "unchecked
    // notifications".
    // But if one admin checks it, does it disappear for others? Usually yes for
    // "Work Items" but no for "Personal Alerts".
    // Let's assume it's a "Work Item" (Approval Request). So shared.
    // However, the table name is `notifications`.
    // Let's add an `type` field to distinguish.

    private String type; // e.g., "SIGNUP_REQUEST", "SYSTEM_ALERT"
}
