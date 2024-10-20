package main.handlers;

import main.reports.ReportRequest;
import main.strategies.report.ReportFormattingStrategyImpl;

public class DataFormattingHandler extends AbstractReportHandler{
    private ReportFormattingStrategyImpl formattingStrategy;

    public DataFormattingHandler(ReportFormattingStrategyImpl formattingStrategy) {
        this.formattingStrategy = formattingStrategy;
    }

    @Override
    protected void process(ReportRequest request) {
        if (request.shouldContinue()) {
            formattingStrategy.formatData(request);
        }
    }
}
