package com.example.demo.src.feed;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.model.GetFeedRes;
import com.example.demo.src.feed.model.PatchFeedReq;
import com.example.demo.src.feed.model.PostFeedReq;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.example.demo.common.response.BaseResponseStatus.PAGINATION_PARAM_INVALID;

@Tag(name = "feed", description = "피드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/feeds")
public class FeedController {

    private final FeedService feedService;
    private final JwtService jwtService;

    /**
     * 피드 목록 조회 API
     * [GET] /app/feeds
     * @return BaseResponse<List<GetFeedRes>>
     */
    @Operation(summary = "피드 목록 조회", description = "pageIndex와 size를 받아 최신순으로 피드를 페이지네이션합니다.")
    @GetMapping("")
    public BaseResponse<List<GetFeedRes>> getFeeds(
            @RequestParam(required = false) Integer pageIndex,
            @RequestParam(required = false) Integer size
    ) {
        if (pageIndex == null || size == null) throw new BaseException(PAGINATION_PARAM_INVALID);
        if (pageIndex < 0 || size <= 0) throw new BaseException(PAGINATION_PARAM_INVALID);
        return new BaseResponse<>(feedService.getFeeds(pageIndex, size));
    }

    /**
     * 피드 상세 조회 API
     * [GET] /app/feeds/{feedId}
     * @return BaseResponse<GetFeedRes>
     */
    @Operation(summary = "피드 상세 조회", description = "feedId로 피드 단건을 조회합니다.")
    @GetMapping("/{feedId}")
    public BaseResponse<GetFeedRes> getFeed(@PathVariable Long feedId) {
        return new BaseResponse<>(feedService.getFeed(feedId));
    }

    /**
     * 피드 작성 API
     * [POST] /app/feeds
     * @return BaseResponse<Long>
     */
    @Operation(summary = "피드 작성", description = "본문과 이미지를 받아 피드를 생성합니다.")
    @PostMapping("")
    public BaseResponse<Long> createFeed(@RequestBody PostFeedReq req) {
        Long userId = jwtService.getUserId();
        Long id = feedService.createFeed(userId, req);
        return new BaseResponse<>(id);
    }

    /**
     * 피드 수정 API
     * [PATCH] /app/feeds/{feedId}
     * @return BaseResponse<String>
     */
    @PatchMapping("{feedId}")
    public BaseResponse<String> updateFeed(@PathVariable Long feedId, @RequestBody PatchFeedReq req) {
        Long userId = jwtService.getUserId();
        feedService.updateFeed(feedId, userId, req);
        return new BaseResponse<>("수정 완료");
    }

    /**
     * 피드 삭제 API
     * [DELETE] /app/feeds/{feedId}
     * @return BaseResponse<String>
     */
    @Operation(summary = "피드 삭제", description = "작성자가 피드를 삭제합니다.")
    @DeleteMapping("/{feedId}")
    public BaseResponse<String> deleteFeed(@Parameter(description = "피드 ID") @PathVariable Long feedId) {
        Long userId = jwtService.getUserId();
        feedService.deleteFeed(feedId, userId);
        return new BaseResponse<>("삭제 완료");
    }
}
