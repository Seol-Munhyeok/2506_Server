package com.example.demo.src.feed;

import com.example.demo.src.feed.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    long countByFeedId(Long feedId);
}