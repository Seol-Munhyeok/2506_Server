package com.example.demo.src.comment;

import com.example.demo.src.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByFeedId(Long feedId, Pageable pageable);
    void deleteAllByFeedId(Long feedId);
}
