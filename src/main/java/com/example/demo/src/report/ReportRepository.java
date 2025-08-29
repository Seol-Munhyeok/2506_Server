package com.example.demo.src.report;

import com.example.demo.src.report.entity.Report;
import com.example.demo.src.report.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByReportedFeedIdAndStatus(Long feedId, ReportStatus status);

    List<Report> findAllByStatus(ReportStatus status);
}
