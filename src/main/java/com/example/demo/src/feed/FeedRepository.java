package com.example.demo.src.feed;

import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.entity.FeedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    Page<Feed> findAllByStatusOrderByCreatedAtDesc(FeedStatus status, Pageable pageable);
}
