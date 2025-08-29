package com.example.demo.src.report;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.report.model.PostReportReq;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "report", description = "신고 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/feeds")
public class ReportController {

    private final ReportService reportService;
    private final JwtService jwtService;

    @Operation(summary = "피드 신고", description = "피드를 신고합니다.")
    @PostMapping("/{feedId}/report")
    public BaseResponse<Long> reportFeed(@PathVariable Long feedId, @RequestBody PostReportReq req) {
        Long userId = jwtService.getUserId();
        return new BaseResponse<>(reportService.reportFeed(feedId, userId, req));
    }
}
