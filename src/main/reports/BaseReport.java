package main.reports;

public abstract class BaseReport implements Report { 
    private ReportHandler initialHandler;
    private ReportRequest request;

    public BaseReport(ReportHandler initialHandler) {
        this.initialHandler = initialHandler;
    }

    @Override
    public final void generateReport() {
        if (request == null) {
            request = new ReportRequest(getReportType());
        } else {
            request.reset(getReportType());
        }
        customizeReportRequest(request); // Allow subclasses to customize the report request
        initialHandler.handleRequest(request);
        if (!request.shouldContinue()) {
            request.setProcessedData("formattedData", "No data to display.");
        }
    }

    protected abstract String getReportType();

    protected void customizeReportRequest(ReportRequest request) {}

    protected ReportRequest getRequest() {
        return request;
    }
}
