package main.handlers;

import main.reports.ReportRequest;
import main.strategies.report.DatabaseReportFetchingStrategyImpl;

public class DataFetchingHandler extends AbstractReportHandler {
    private DatabaseReportFetchingStrategyImpl fetchingStrategy;

    public DataFetchingHandler(DatabaseReportFetchingStrategyImpl fetchingStrategy) {
        this.fetchingStrategy = fetchingStrategy;
    }

    @Override
    protected void process(ReportRequest request) {
        fetchingStrategy.fetchData(request);
    }
}
