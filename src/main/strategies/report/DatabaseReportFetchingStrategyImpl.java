package main.strategies.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.domain.dao.StockDAO;
import main.domain.model.*;
import main.reports.ReportRequest;

public class DatabaseReportFetchingStrategyImpl implements FetchingStrategy {
    private static final String DAILY_SALES_QUERY = "SELECT t.*, b.bill_date, i.item_name, (i.item_price * t.quantity) AS total_price "
            + "FROM Transactions t "
            + "JOIN Bills b ON t.bill_id = b.bill_id "
            + "JOIN Items i ON t.item_id = i.item_code "
            + "WHERE b.bill_date = ?";
    private static final String RESHELVED_ITEMS_QUERY = "SELECT re.ItemCode, SUM(re.Quantity) AS total_quantity, i.item_name "
            + "FROM ReshelvingEvents re "
            + "JOIN Items i ON re.ItemCode = i.item_code "
            + "WHERE DATE(re.ReshelvingTimestamp) = ? "
            + "GROUP BY re.ItemCode, i.item_name";
    private static final String BILL_QUERY = "SELECT bill_id, bill_date, total_price, discount_amount, tax_amount, final_price, cash_tendered, change_amount "
            + "FROM Bills WHERE bill_date = ?";
    private static final String ALL_BILLS_QUERY = "SELECT bill_id, bill_date, total_price, discount_amount, tax_amount, final_price, cash_tendered, change_amount "
            + "FROM Bills";

    private final Connection connection;
    private final StockDAO stockDAO;

    public DatabaseReportFetchingStrategyImpl(Connection connection, StockDAO stockDAO) {
        this.connection = connection;
        this.stockDAO = stockDAO;
    }

    @Override
    public void fetchData(ReportRequest request) {
        String reportDate = (String) request.getParameter("reportDate");

        switch (request.getReportType()) {
            case "DailySales":
                fetchDailySalesData(request, reportDate);
                break;
            case "ReshelvedItems":
                fetchReshelvedItemsData(request, reportDate);
                break;
            case "ReorderStock":
                fetchReorderStockData(request);
                break;
            case "Stock":
                fetchStockData(request);
                break;
            case "Bill":
                fetchBillData(request, reportDate);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + request.getReportType());
        }
    }

    private void fetchDailySalesData(ReportRequest request, String reportDate) {
        executeQuery(request, DAILY_SALES_QUERY, reportDate, rs -> {
            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) {
                Transaction transaction = new TransactionBuilder()
                        .setId(rs.getInt("transaction_id"))
                        .setBillId(rs.getInt("bill_id"))
                        .setItemId(rs.getString("item_id"))
                        .setQuantity(rs.getInt("quantity"))
                        .setTotalPrice(rs.getDouble("total_price"))
                        .setItemName(rs.getString("item_name"))
                        .build();
                transactions.add(transaction);
            }
            request.setTransactions(transactions);
            if (transactions.isEmpty()) {
                request.setShouldContinue(false);
            }
        });
    }

    private void fetchReshelvedItemsData(ReportRequest request, String reportDate) {
        executeQuery(request, RESHELVED_ITEMS_QUERY, reportDate, rs -> {
            List<ReshelvedItemLogger> reshelvedItems = new ArrayList<>();
            while (rs.next()) {
                ReshelvedItemLogger item = new ReshelvedItemLogger();
                item.setItemCode(rs.getString("ItemCode"));
                item.setQuantity(rs.getInt("total_quantity"));
                item.setItemName(rs.getString("item_name"));
                reshelvedItems.add(item);
            }
            request.setProcessedData("reshelvedItems", reshelvedItems);
            if (reshelvedItems.isEmpty()) {
                request.setShouldContinue(false);
            }
        });
    }

    private void fetchReorderStockData(ReportRequest request) {
        List<ItemStock> items = stockDAO.findStockBelowReorderLevel(50);
        request.setProcessedData("reorderStockItems", items);
        if (items.isEmpty()) {
            request.setShouldContinue(false);
        }
    }

    private void fetchStockData(ReportRequest request) {
        List<ItemStock> items = stockDAO.findAllStockDetails();
        request.setProcessedData("stockItems", items);
        if (items.isEmpty()) {
            request.setShouldContinue(false);
        }
    }

    private void fetchBillData(ReportRequest request, String reportDate) {
        String query = reportDate == null ? ALL_BILLS_QUERY : BILL_QUERY;
        executeQuery(request, query, reportDate, rs -> {
            List<Bill> bills = new ArrayList<>();
            while (rs.next()) {
                Bill bill = new Bill.BillBuilder()
                        .withBillId(rs.getInt("bill_id"))
                        .withBillDate(rs.getDate("bill_date"))
                        .withTotalAmount(rs.getDouble("total_price"))
                        .withDiscountAmount(rs.getDouble("discount_amount"))
                        .withTaxAmount(rs.getDouble("tax_amount"))
                        .withFinalPrice(rs.getDouble("final_price"))
                        .withCashTendered(rs.getDouble("cash_tendered"))
                        .withChangeGiven(rs.getDouble("change_amount"))
                        .build();
                bills.add(bill);
            }
            request.setProcessedData("bills", bills);
            if (bills.isEmpty()) {
                request.setShouldContinue(false);
            }
        });
    }

    private void executeQuery(ReportRequest request, String query, String param, ResultSetHandler handler) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (param != null) {
                stmt.setString(1, param);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                handler.map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface ResultSetHandler {
        void map(ResultSet rs) throws SQLException;
    }
}
