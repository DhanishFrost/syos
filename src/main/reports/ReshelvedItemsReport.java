package main.reports;

public class ReshelvedItemsReport extends BaseReport {
    private String reportDate;

    public ReshelvedItemsReport(ReportHandler initialHandler) {
        super(initialHandler);
    }

    @Override
    protected String getReportType() {
        return "ReshelvedItems";
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
