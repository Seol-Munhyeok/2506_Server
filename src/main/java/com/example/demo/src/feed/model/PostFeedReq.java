package com.example.demo.src.feed.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostFeedReq {
    private String content;
    private List<String> imageUrls;
}
