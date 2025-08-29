package com.example.demo.src.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetFeedRes {
    private Long feedId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private AuthorProfile authorProfile;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private Long likeCount;
}