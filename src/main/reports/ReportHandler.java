package main.reports;

public interface ReportHandler {
    void setNextHandler(ReportHandler nextHandler);
    void handleRequest(ReportRequest request);
}
