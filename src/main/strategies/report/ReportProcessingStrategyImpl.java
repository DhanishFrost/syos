package main.strategies.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;

import main.domain.model.*;
import main.reports.*;

public class ReportProcessingStrategyImpl implements ProcessingStrategy {
    @Override
    public void processData(ReportRequest request) {
        String reportType = request.getReportType();

        switch (reportType) {
            case "DailySales":
                processDailySalesData(request);
                break;
            case "ReshelvedItems":
                processReshelvedItemsData(request);
                break;
            case "ReorderStock":
                processReorderStockData(request);
                break;
            case "Stock":
                processStockData(request);
                break;
            case "Bill":
                processBillData(request);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    private void processDailySalesData(ReportRequest request) {
        List<Transaction> transactions = request.getTransactions();
        if (transactions == null) {
            request.setProcessedData("itemQuantities", new HashMap<>());
            request.setProcessedData("itemRevenues", new HashMap<>());
            request.setProcessedData("itemNames", new HashMap<>());
            request.setProcessedData("totalRevenue", 0.0);
            return;
        }

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemRevenues = new HashMap<>();
        Map<String, String> itemNames = new HashMap<>();
        double totalRevenue = 0.0;

        for (Transaction transaction : transactions) {
            String itemCode = transaction.getItemId();
            String itemName = transaction.getItemName();
            int quantity = transaction.getQuantity();
            double totalPrice = transaction.getTotalPrice();

            itemQuantities.put(itemCode, itemQuantities.getOrDefault(itemCode, 0) + quantity);
            itemRevenues.put(itemCode, itemRevenues.getOrDefault(itemCode, 0.0) + totalPrice);
            itemNames.put(itemCode, itemName);
            totalRevenue += totalPrice;
        }

        request.setProcessedData("itemQuantities", itemQuantities);
        request.setProcessedData("itemRevenues", itemRevenues);
        request.setProcessedData("itemNames", itemNames);
        request.setProcessedData("totalRevenue", totalRevenue);
    }

    private void processReshelvedItemsData(ReportRequest request) {
        @SuppressWarnings("unchecked")
        List<ReshelvedItemLogger> reshelvedItems = (List<ReshelvedItemLogger>) request.getProcessedData("reshelvedItems");
        if (reshelvedItems == null) {
            request.setProcessedData("itemQuantities", new HashMap<>());
            request.setProcessedData("itemNames", new HashMap<>());
            request.setProcessedData("totalReshelved", 0.0);
            return;
        }

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, String> itemNames = new HashMap<>();
        double totalReshelved = 0.0;

        for (ReshelvedItemLogger item : reshelvedItems) {
            String itemCode = item.getItemCode();
            String itemName = item.getItemName();
            int quantity = item.getQuantity();

            itemQuantities.put(itemCode, itemQuantities.getOrDefault(itemCode, 0) + quantity);
            itemNames.put(itemCode, itemName);
            totalReshelved += quantity;
        }

        request.setProcessedData("itemQuantities", itemQuantities);
        request.setProcessedData("itemNames", itemNames);
        request.setProcessedData("totalReshelved", totalReshelved);
    }

    private void processReorderStockData(ReportRequest request) {
        @SuppressWarnings("unchecked")
        List<ItemStock> items = (List<ItemStock>) request.getProcessedData("reorderStockItems");
        if (items == null) {
            request.setProcessedData("itemQuantities", new HashMap<>());
            request.setProcessedData("itemNames", new HashMap<>());
            request.setProcessedData("totalItems", 0.0);
            return;
        }

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, String> itemNames = new HashMap<>();
        double totalItems = 0.0;

        for (ItemStock item : items) {
            String itemCode = item.getCode();
            String itemName = item.getName();
            int quantity = item.getQuantity();

            itemQuantities.put(itemCode, itemQuantities.getOrDefault(itemCode, 0) + quantity);
            itemNames.put(itemCode, itemName);
            totalItems += quantity;
        }

        request.setProcessedData("itemQuantities", itemQuantities);
        request.setProcessedData("itemNames", itemNames);
        request.setProcessedData("totalItems", totalItems);
    }

    private void processStockData(ReportRequest request) {
        @SuppressWarnings("unchecked")
        List<ItemStock> items = (List<ItemStock>) request.getProcessedData("stockItems");

        if (items == null) {
            request.setProcessedData("processedStockItems", new ArrayList<ItemStock>());
            return;
        }

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, String> itemNames = new HashMap<>();
        Map<String, Date> itemPurchaseDates = new HashMap<>();
        Map<String, Date> itemExpiryDates = new HashMap<>();

        for (ItemStock item : items) {
            String itemCode = item.getCode();
            String itemName = item.getName();
            int quantity = item.getQuantity();
            Date dateOfPurchase = item.getStockDateOfPurchase();
            Date expiryDate = item.getStockExpiryDate();

            itemQuantities.put(itemCode, itemQuantities.getOrDefault(itemCode, 0) + quantity);
            itemNames.put(itemCode, itemName);
            itemPurchaseDates.put(itemCode, dateOfPurchase);
            itemExpiryDates.put(itemCode, expiryDate);
        }

        request.setProcessedData("itemQuantities", itemQuantities);
        request.setProcessedData("itemNames", itemNames);
        request.setProcessedData("itemPurchaseDates", itemPurchaseDates);
        request.setProcessedData("itemExpiryDates", itemExpiryDates);
        request.setProcessedData("processedStockItems", items);
    }

    private void processBillData(ReportRequest request) {
        @SuppressWarnings("unchecked")
        List<Bill> bills = (List<Bill>) request.getProcessedData("bills");
        if (bills == null) {
            request.setProcessedData("processedBills", new ArrayList<Bill>());
            return;
        }
        request.setProcessedData("processedBills", bills);
    }
}
