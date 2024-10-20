package main.strategies.report;

import main.reports.ReportRequest;

public interface FetchingStrategy {
    void fetchData(ReportRequest request);
}
