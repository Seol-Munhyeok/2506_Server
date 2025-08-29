package com.example.demo.src.like;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "like", description = "좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/feeds")
public class LikeController {

    private final LikeService likeService;
    private final JwtService jwtService;

    @Operation(summary = "좋아요 토글", description = "좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{feedId}/like")
    public BaseResponse<Long> toggleLike(@PathVariable Long feedId) {
        Long userId = jwtService.getUserId();
        Long count = likeService.toggleLike(feedId, userId);
        return new BaseResponse<>(count);
    }

    @Operation(summary = "좋아요 취소", description = "좋아요를 취소합니다.")
    @DeleteMapping("/{feedId}/like")
    public BaseResponse<Long> cancelLike(@PathVariable Long feedId) {
        Long userId = jwtService.getUserId();
        Long count = likeService.cancelLike(feedId, userId);
        return new BaseResponse<>(count);
    }
}
