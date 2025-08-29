package com.example.demo.src.like;

import com.example.demo.src.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByFeedId(Long feedId);
    boolean existsByFeedIdAndUserId(Long feedId, Long userId);
    void deleteByFeedIdAndUserId(Long feedId, Long userId);
    Optional<Like> findByFeedIdAndUserId(Long feedId, Long userId);
}
