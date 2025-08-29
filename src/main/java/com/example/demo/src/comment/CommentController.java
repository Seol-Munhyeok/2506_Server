package com.example.demo.src.comment;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.comment.model.GetCommentRes;
import com.example.demo.src.comment.model.PostCommentReq;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.common.response.BaseResponseStatus.PAGINATION_PARAM_INVALID;

@Tag(name = "comment", description = "댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/feeds")
public class CommentController {

    private final CommentService commentService;
    private final JwtService jwtService;

    @Operation(summary = "댓글 목록 조회", description = "pageIndex와 size를 받아 댓글을 페이지네이션합니다. 기본 2개를 반환합니다.")
    @GetMapping("/{feedId}/comments")
    public BaseResponse<List<GetCommentRes>> getComments(
            @PathVariable Long feedId,
            @RequestParam(required = false) Integer pageIndex,
            @RequestParam(required = false) Integer size
    ) {
        int p = pageIndex == null ? 0 : pageIndex;
        int s = size == null ? 2 : size;
        if (p < 0 || s <= 0) throw new BaseException(PAGINATION_PARAM_INVALID);
        return new BaseResponse<>(commentService.getComments(feedId, p, s));
    }

    @Operation(summary = "댓글 작성", description = "댓글을 작성합니다.")
    @PostMapping("/{feedId}/comments")
    public BaseResponse<GetCommentRes> createComment(
            @PathVariable Long feedId,
            @RequestBody PostCommentReq req
    ) {
        Long userId = jwtService.getUserId();
        return new BaseResponse<>(commentService.createComment(feedId, userId, req));
    }
}