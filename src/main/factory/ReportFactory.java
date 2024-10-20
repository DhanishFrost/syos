package main.factory;

import java.sql.Connection;

import main.domain.dao.StockDAO;
import main.handlers.*;
import main.strategies.report.*;
import main.reports.*;

public class ReportFactory {
    private Connection connection;
    private StockDAO stockDAO;

    public ReportFactory(Connection connection, StockDAO stockDAO) {
        this.connection = connection;
        this.stockDAO = stockDAO;
    }

    private ReportHandler createReportHandlerChain() {
        // Initialize strategy
        DatabaseReportFetchingStrategyImpl fetchingStrategy = new DatabaseReportFetchingStrategyImpl(connection, stockDAO);
        ReportProcessingStrategyImpl processingStrategy = new ReportProcessingStrategyImpl();
        ReportFormattingStrategyImpl formattingStrategy = new ReportFormattingStrategyImpl();

        // Initialize handlers
        ReportHandler dataFetchingHandler = new DataFetchingHandler(fetchingStrategy);
        ReportHandler dataProcessingHandler = new DataProcessingHandler(processingStrategy);
        ReportHandler dataFormattingHandler = new DataFormattingHandler(formattingStrategy);

        // Link handlers
        dataFetchingHandler.setNextHandler(dataProcessingHandler);
        dataProcessingHandler.setNextHandler(dataFormattingHandler);

        return dataFetchingHandler; // Return the first handler in the chain
    }

    public Report createReport(String reportType) {
        ReportHandler handlerChain = createReportHandlerChain();
        switch (reportType) {
            case "Bill":
                return new BillReport(handlerChain);
            case "DailySales":
                return new DailySalesReport(handlerChain);
            case "ReorderStock":
                return new ReorderStockReport(handlerChain);
            case "ReshelvedItems":
                return new ReshelvedItemsReport(handlerChain);
            case "Stock":
                return new StockReport(handlerChain);
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    public ReportHandler getReportHandlerChain() {
        return createReportHandlerChain();
    }
}
