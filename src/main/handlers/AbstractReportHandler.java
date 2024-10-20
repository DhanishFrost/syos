package main.handlers;

import main.reports.ReportHandler;
import main.reports.ReportRequest;

public abstract class AbstractReportHandler implements ReportHandler {
    protected ReportHandler nextHandler;

    @Override
    public void setNextHandler(ReportHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(ReportRequest request) {
        if (!request.shouldContinue()) {
            System.out.println("Stopping the chain as there are no transactions.");
            return;
        }
        process(request);
        if (nextHandler != null && request.shouldContinue()) {
            nextHandler.handleRequest(request);
        }
    }

    protected abstract void process(ReportRequest request);
}
