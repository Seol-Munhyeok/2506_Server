package com.example.demo.src.report;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.src.feed.FeedRepository;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.entity.FeedStatus;
import com.example.demo.src.report.entity.Report;
import com.example.demo.src.report.entity.ReportCategory;
import com.example.demo.src.report.entity.ReportStatus;
import com.example.demo.src.report.model.PostReportReq;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.common.response.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    private static final int REPORT_THRESHOLD = 5;


    @Transactional
    public Long reportFeed(Long feedId, Long reporterId, PostReportReq req) {
        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            throw new BaseException(REPORTS_EMPTY_REASON);
        }
        if (req.getCategory() == null) {
            throw new BaseException(REPORTS_EMPTY_CATEGORY);
        }
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (feed.getStatus() != FeedStatus.ACTIVE) throw new BaseException(NOT_FIND_FEED);
        if (feed.getUser().getId().equals(reporterId)) {
            throw new BaseException(REPORTS_SELF_NOT_ALLOWED);
        }
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        Report report = Report.builder()
                .reporter(reporter)
                .reportedFeed(feed)
                .reportReason(req.getReason())
                .category(req.getCategory())
                .status(ReportStatus.PENDING)
                .build();
        reportRepository.save(report);
        checkAndDeactivateFeed(feedId);
        return report.getId();
    }

    @Transactional
    public void checkAndDeactivateFeed(Long feedId) {
        long count = reportRepository.countByReportedFeedIdAndStatus(feedId, ReportStatus.PENDING);
        if (count >= REPORT_THRESHOLD) {
            Feed feed = feedRepository.findById(feedId)
                    .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
            feed.changeStatus(FeedStatus.INACTIVE);
        }
    }

}

