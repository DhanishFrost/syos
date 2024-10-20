package main.strategies.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import main.reports.ReportRequest;
import main.domain.model.Bill;
import main.domain.model.ItemStock;

public class ReportFormattingStrategyImpl implements FormattingStrategy {
    private final Map<String, DataFormatter> formatters = new HashMap<>();

    public ReportFormattingStrategyImpl() {
        formatters.put("DailySales", this::formatDailySalesData);
        formatters.put("ReshelvedItems", this::formatReshelvedItemsData);
        formatters.put("ReorderStock", this::formatReorderStockData);
        formatters.put("Stock", this::formatStockData);
        formatters.put("Bill", this::formatBillData);
    }

    @Override
    public void formatData(ReportRequest request) {
        String reportType = request.getReportType();
        DataFormatter formatter = formatters.get(reportType);
        if (formatter != null) {
            formatter.format(request);
        } else {
            throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    private void formatDailySalesData(ReportRequest request) {
        Map<String, Integer> itemQuantities = getProcessedData(request, "itemQuantities");
        Map<String, Double> itemRevenues = getProcessedData(request, "itemRevenues");
        Map<String, String> itemNames = getProcessedData(request, "itemNames");
        double totalRevenue = (double) request.getProcessedData("totalRevenue");

        String[] headers = {"Code", "Name", "Quantity", "Revenue"};
        List<String[]> rows = itemQuantities.keySet().stream()
                .map(code -> new String[]{
                        code,
                        itemNames.get(code),
                        itemQuantities.get(code).toString(),
                        String.format("%.2f", itemRevenues.get(code))
                })
                .collect(Collectors.toList());

        String formattedData = formatTable("Daily Sales Report", headers, rows, "Total Revenue:", String.format("%.2f", totalRevenue));
        request.setProcessedData("formattedData", formattedData);
    }

    private void formatReshelvedItemsData(ReportRequest request) {
        Map<String, Integer> itemQuantities = getProcessedData(request, "itemQuantities");
        Map<String, String> itemNames = getProcessedData(request, "itemNames");
        double totalReshelved = (double) request.getProcessedData("totalReshelved");

        String[] headers = {"Code", "Name", "Quantity"};
        List<String[]> rows = itemQuantities.keySet().stream()
                .map(code -> new String[]{
                        code,
                        itemNames.get(code),
                        itemQuantities.get(code).toString()
                })
                .collect(Collectors.toList());

        String formattedData = formatTable("Reshelved Items Report", headers, rows, "Total Reshelved:", String.format("%.2f", totalReshelved));
        request.setProcessedData("formattedData", formattedData);
    }

    private void formatReorderStockData(ReportRequest request) {
        Map<String, Integer> itemQuantities = getProcessedData(request, "itemQuantities");
        Map<String, String> itemNames = getProcessedData(request, "itemNames");
        double totalItems = (double) request.getProcessedData("totalItems");

        String[] headers = {"Code", "Name", "Quantity"};
        List<String[]> rows = itemQuantities.keySet().stream()
                .map(code -> new String[]{
                        code,
                        itemNames.get(code),
                        itemQuantities.get(code).toString()
                })
                .collect(Collectors.toList());

        String formattedData = formatTable("Reorder Stock Report", headers, rows, "Available Quantity:", String.format("%.2f", totalItems));
        request.setProcessedData("formattedData", formattedData);
    }

    private void formatStockData(ReportRequest request) {
        List<ItemStock> items = getProcessedData(request, "processedStockItems");

        String[] headers = {"Code", "Name", "Date of Purchase", "Quantity", "Expiry Date"};
        List<String[]> rows = items.stream()
                .map(item -> new String[]{
                        item.getCode(),
                        item.getName(),
                        item.getStockDateOfPurchase().toString(),
                        String.valueOf(item.getQuantity()),
                        item.getStockExpiryDate().toString()
                })
                .collect(Collectors.toList());

        String formattedData = formatTable("Stock Report", headers, rows, null, null);
        request.setProcessedData("formattedData", formattedData);
    }

    private void formatBillData(ReportRequest request) {
        List<Bill> bills = getProcessedData(request, "processedBills");

        String[] headers = {"Bill ID", "Bill Date", "Total Price", "Discount", "Tax", "Final Price", "Cash Tendered", "Change"};
        List<String[]> rows = bills.stream()
                .map(bill -> new String[]{
                        String.valueOf(bill.getBillId()),
                        bill.getBillDate().toString(),
                        String.format("%.2f", bill.getTotalAmount()),
                        String.format("%.2f", bill.getDiscountAmount()),
                        String.format("%.2f", bill.getTaxAmount()),
                        String.format("%.2f", bill.getFinalPrice()),
                        String.format("%.2f", bill.getCashTendered()),
                        String.format("%.2f", bill.getChangeGiven())
                })
                .collect(Collectors.toList());

        String formattedData = formatTable("Bill Report", headers, rows, null, null);
        request.setProcessedData("formattedData", formattedData);
    }

    @SuppressWarnings("unchecked")
    private <T> T getProcessedData(ReportRequest request, String key) {
        return (T) request.getProcessedData(key);
    }

    private String formatTable(String title, String[] headers, List<String[]> rows, String footerLabel, String footerValue) {
        StringBuilder formattedData = new StringBuilder();
        int totalWidth = headers.length * 17;

        formattedData.append(title).append("\n");
        formattedData.append("-".repeat(totalWidth)).append("\n");

        // Create the format string dynamically based on the number of headers
        String headerFormat = createFormatString(headers.length);
        formattedData.append(String.format(headerFormat, (Object[]) headers));
        formattedData.append("-".repeat(totalWidth)).append("\n");

        for (String[] row : rows) {
            formattedData.append(String.format(headerFormat, (Object[]) row));
        }

        if (footerLabel != null && footerValue != null) {
            formattedData.append("-".repeat(totalWidth)).append("\n");
            formattedData.append(String.format("%-" + (totalWidth - 17) + "s%" + 17 + "s\n", footerLabel, footerValue));
        }

        return formattedData.toString();
    }

    private String createFormatString(int columnCount) {
        StringBuilder formatString = new StringBuilder();
        for (int i = 0; i < columnCount; i++) {
            formatString.append("%-17s");
        }
        formatString.append("\n");
        return formatString.toString();
    }

    @FunctionalInterface
    private interface DataFormatter {
        void format(ReportRequest request);
    }
}
