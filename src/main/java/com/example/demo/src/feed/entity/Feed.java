package com.example.demo.src.feed.entity;

import com.example.demo.common.entity.BaseEntity;
import com.example.demo.src.user.entity.User;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "feeds")
public class Feed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private FeedStatus status;

    @Builder
    public Feed(Long id, User user, String content, FeedStatus status) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.status = status;
    }
}
