package main.strategies.report;

import main.reports.ReportRequest;

public interface ProcessingStrategy {
    void processData(ReportRequest request);
}