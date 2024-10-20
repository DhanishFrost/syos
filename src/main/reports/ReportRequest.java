package main.reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.domain.model.Transaction;

import java.util.ArrayList;

public class ReportRequest {
    private String reportType;
    private Map<String, Object> parameters;
    private List<Transaction> transactions;
    private Map<String, Object> processedData;
    private boolean shouldContinue;

    public ReportRequest(String reportType) {
        this.reportType = reportType;
        this.parameters = new HashMap<>();
        this.processedData = new HashMap<>();
        this.shouldContinue = true;
        this.transactions = new ArrayList<>();
    }

    public String getReportType() {
        return reportType;
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setProcessedData(String key, Object value) {
        processedData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProcessedData(String key) {
        return (T) processedData.get(key);
    }

    public boolean shouldContinue() {
        return shouldContinue;
    }

    public void setShouldContinue(boolean shouldContinue) {
        this.shouldContinue = shouldContinue;
    }

    public void reset(String reportType) {
        this.reportType = reportType;
        this.parameters.clear();
        this.processedData.clear();
        this.transactions = null;
        this.shouldContinue = true;
    }
}