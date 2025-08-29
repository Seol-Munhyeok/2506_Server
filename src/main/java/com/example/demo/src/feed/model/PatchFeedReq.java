package com.example.demo.src.feed.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PatchFeedReq {
    private String content;
    private List<String> imageUrls;
}
