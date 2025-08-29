package com.example.demo.src.feed;

import com.example.demo.src.feed.entity.FeedMedia;
import com.example.demo.src.feed.entity.FeedMediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedMediaRepository extends JpaRepository<FeedMedia, Long> {
    List<FeedMedia> findAllByFeedId(Long feedId);
    List<FeedMedia> findAllByFeedIdAndMediaType(Long feedId, FeedMediaType mediaType);
}