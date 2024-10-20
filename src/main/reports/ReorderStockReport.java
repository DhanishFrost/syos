package main.reports;

public class ReorderStockReport extends BaseReport {

    public ReorderStockReport(ReportHandler initialHandler) {
        super(initialHandler);
    }

    @Override
    protected String getReportType() {
        return "ReorderStock";
    }

    @Override
    public String displayReport() {
        ReportRequest request = getRequest();
        String formattedData = request.getProcessedData("formattedData");
        System.out.println(formattedData);
        return formattedData;
    }
}
