package com.example.demo.src.feed.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@NoArgsConstructor
public class PatchFeedReq {
    @NotBlank(message = "본문을 입력해주세요.")
    @Size(max = 1000, message = "본문은 1자 이상 1000자 이하여야 합니다.")
    private String content;
    private List<String> imageUrls;
    private List<String> videoUrls;
}
