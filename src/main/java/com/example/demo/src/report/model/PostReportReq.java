package com.example.demo.src.report.model;

import com.example.demo.src.report.entity.ReportCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostReportReq {
    private ReportCategory category;
    private String reason;
}
