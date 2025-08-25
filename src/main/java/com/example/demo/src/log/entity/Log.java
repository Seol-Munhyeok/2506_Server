package com.example.demo.src.log.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false, updatable = false)
    private Long id;

    @Column(length = 50, nullable = false)
    private String domain;

    @Column(length = 20, nullable = false)
    private String action;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Lob
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Log(Long id, String domain, String action, Long entityId, String message) {
        this.id = id;
        this.domain = domain;
        this.action = action;
        this.entityId = entityId;
        this.message = message;
    }
}
