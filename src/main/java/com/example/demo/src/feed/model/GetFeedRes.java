package com.example.demo.src.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GetFeedRes {
    private Long feedId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}