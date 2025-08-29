package com.example.demo.src.feed;

import com.example.demo.src.feed.entity.FeedImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedImageRepository extends JpaRepository<FeedImage, Long> {
    List<FeedImage> findAllByFeedId(Long feedId);
}
