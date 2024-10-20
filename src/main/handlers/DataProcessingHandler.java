package main.handlers;

import main.reports.ReportRequest;
import main.strategies.report.ReportProcessingStrategyImpl;

public class DataProcessingHandler extends AbstractReportHandler {
    private ReportProcessingStrategyImpl processingStrategy;

    public DataProcessingHandler(ReportProcessingStrategyImpl processingStrategy) {
        this.processingStrategy = processingStrategy;
    }

    @Override
    protected void process(ReportRequest request) {
        if (request.shouldContinue()) {
            processingStrategy.processData(request);
        }
    }
}