package com.example.demo.src.report.scheduler;

import com.example.demo.src.report.ReportRepository;
import com.example.demo.src.report.ReportService;
import com.example.demo.src.report.entity.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportRepository reportRepository;
    private final ReportService reportService;

    @Scheduled(fixedDelay = 60000)
    public void monitorReportCounts() {
        reportRepository.findAllByStatus(ReportStatus.PENDING).stream()
                .filter(r -> r.getReportedFeed() != null)
                .map(r -> r.getReportedFeed().getId())
                .distinct()
                .forEach(reportService::checkAndDeactivateFeed);
    }
}
