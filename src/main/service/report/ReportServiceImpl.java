package main.service.report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import main.factory.*;
import main.reports.*;

public class ReportServiceImpl implements ReportService {
    private final ReportFactory reportFactory;

    public ReportServiceImpl(ReportFactory reportFactory) {
        this.reportFactory = reportFactory;
    }

    @Override
    public String handleReportGeneration(Map<String, String[]> parameters) {
        int choice = promptForReportMenuChoice(parameters);
        String dateStr = null;

        Report report = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (choice) {
            case 1:
                dateStr = promptForDate(parameters, "reportDate", formatter);
                report = reportFactory.createReport("DailySales");
                ((DailySalesReport) report).setDate(dateStr);
                break;
            case 2:
                dateStr = promptForDate(parameters, "reportDate", formatter);
                report = reportFactory.createReport("ReshelvedItems");
                ((ReshelvedItemsReport) report).setDate(dateStr);
                break;
            case 3:
                report = reportFactory.createReport("ReorderStock");
                break;
            case 4:
                report = reportFactory.createReport("Stock");
                break;
            case 5:
                dateStr = promptForOptionalDate(parameters, "reportDate", formatter);
                report = reportFactory.createReport("Bill");
                if (dateStr != null) {
                    ((BillReport) report).setDate(dateStr);
                }
                break;
            case 6:
                System.out.println("Returning to main menu.");
                return "Returning to main menu.";
            default:
                System.out.println("Invalid choice. Returning to main menu.");
                return "Invalid choice. Returning to main menu.";
        }

        if (report != null) {
            report.generateReport();
            return report.displayReport(); // Return the generated report as a string
        }

        return "No report generated.";
    }

    private int promptForReportMenuChoice(Map<String, String[]> parameters) {
        String[] choiceArr = parameters.get("reportChoice");
        if (choiceArr != null && choiceArr.length > 0) {
            try {
                int choice = Integer.parseInt(choiceArr[0]);
                if (choice >= 1 && choice <= 6) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number between 1 and 6.");
            }
        }
        return 6; // Default to returning to the main menu
    }

    private String promptForDate(Map<String, String[]> parameters, String paramName, DateTimeFormatter formatter) {
        String[] dateArr = parameters.get(paramName);
        if (dateArr != null && dateArr.length > 0) {
            String dateStr = dateArr[0];
            try {
                LocalDate.parse(dateStr, formatter);
                return dateStr;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter a valid date in the format yyyy-MM-dd.");
            }
        }
        return null;
    }

    private String promptForOptionalDate(Map<String, String[]> parameters, String paramName,
            DateTimeFormatter formatter) {
        String[] dateArr = parameters.get(paramName);
        if (dateArr != null && dateArr.length > 0) {
            String dateStr = dateArr[0];
            if (dateStr.isEmpty()) {
                return null;
            }
            try {
                LocalDate.parse(dateStr, formatter);
                return dateStr;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter a valid date in the format yyyy-MM-dd.");
            }
        }
        return null;
    }
}
