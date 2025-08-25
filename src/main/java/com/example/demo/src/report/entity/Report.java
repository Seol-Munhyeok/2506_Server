package com.example.demo.src.report.entity;

import com.example.demo.common.entity.BaseEntity;
import com.example.demo.src.comment.entity.Comment;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.user.entity.User;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "reports")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_feed_id")
    private Feed reportedFeed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_comment_id")
    private Comment reportedComment;

    @Lob
    @Column(name = "report_reason")
    private String reportReason;

    @Column(length = 20, nullable = false)
    private ReportStatus status;

    @Builder
    public Report(Long id, User reporter, Feed reportedFeed, Comment reportedComment,
                  String reportReason, ReportStatus status) {
        this.id = id;
        this.reporter = reporter;
        this.reportedFeed = reportedFeed;
        this.reportedComment = reportedComment;
        this.reportReason = reportReason;
        this.status = status;
    }
}
