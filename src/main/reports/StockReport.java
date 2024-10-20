package main.reports;

public class StockReport extends BaseReport {
    
    public StockReport(ReportHandler initialHandler) {
        super(initialHandler);
    }

    @Override
    protected String getReportType() {
        return "Stock";
    }

    @Override
    public String displayReport() {
        ReportRequest request = getRequest();
        String formattedData = request.getProcessedData("formattedData");
        System.out.println(formattedData);
        return formattedData;
    }
}
