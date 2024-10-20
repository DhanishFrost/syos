package main.reports;

public class DailySalesReport extends BaseReport {
    private String reportDate;

    public DailySalesReport(ReportHandler initialHandler) {
        super(initialHandler);
    }

    @Override
    protected String getReportType() {
        return "DailySales";
    }

    public void setDate(String date) {
        this.reportDate = date;
        
    }

    @Override
    protected void customizeReportRequest(ReportRequest request) {
        if (reportDate != null) {
            request.setParameter("reportDate", reportDate);
        }
    }

    @Override
    public String displayReport() {
        ReportRequest request = getRequest();
        String formattedData = request.getProcessedData("formattedData");
        System.out.println(formattedData);
        return formattedData;
    }
}

