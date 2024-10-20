package main.strategies.report;

import main.reports.ReportRequest;

public interface FormattingStrategy {
    void formatData(ReportRequest request);
}
