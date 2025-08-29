package com.example.demo.src.comment.model;

import com.example.demo.src.feed.model.AuthorProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GetCommentRes {
    private Long commentId;
    private AuthorProfile author;
    private String content;
    private LocalDateTime createdAt;
}