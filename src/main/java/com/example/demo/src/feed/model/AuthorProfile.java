package com.example.demo.src.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorProfile {
    private Long userId;
    private String loginId;
    private String name;
}
