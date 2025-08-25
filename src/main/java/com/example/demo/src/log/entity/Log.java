package com.example.demo.src.log.entity;

import com.example.demo.common.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@Getter
@Entity
@Table(name = "logs")
public class Log extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private LogDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private LogAction action;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Lob
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Log(Long id, LogDomain domain, LogAction action, Long entityId, String message) {
        this.id = id;
        this.domain = domain;
        this.action = action;
        this.entityId = entityId;
        this.message = message;
    }
}
