package com.example.demo.src.feed.entity;

import com.example.demo.common.entity.BaseEntity;
import lombok.*;
import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "feed_images")
public class FeedImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_image_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Builder
    public FeedImage(Long id, Feed feed, String imageUrl) {
        this.id = id;
        this.feed = feed;
        this.imageUrl = imageUrl;
    }
}
