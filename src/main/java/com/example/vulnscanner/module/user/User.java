package com.example.vulnscanner.module.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Deprecated // Use RoleTemplate instead
    private String role; // ADMIN, USER - Kept for backward compatibility or simple checks

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_template_id")
    private RoleTemplate roleTemplate;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, WITHDRAWN

    private String name;
    private String team;
    private String email;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int failedAttempts = 0;

    private LocalDateTime lockTime;
}