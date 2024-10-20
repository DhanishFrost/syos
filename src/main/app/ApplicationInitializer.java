package main.app;

import java.sql.Connection;

import main.observer.StockReplenishmentObserver;
import static main.app.ApplicationConstants.*;

import main.scheduler.DailyStockChecker;
import main.scheduler.DatabaseReshelvingEventLogger;
import main.scheduler.EndOfDayShelfReplenisher;
import main.scheduler.ExpirationCheckScheduler;
import main.scheduler.ExpirationCheckTask;
import main.scheduler.ReplenishShelvesImpl;
import main.scheduler.ReshelvingEventLogger;
import main.service.billing.BillingService;
import main.service.billing.BillingServiceImpl;
import main.service.customer.CustomerService;
import main.service.customer.CustomerServiceImpl;
import main.service.report.ReportService;
import main.service.report.ReportServiceImpl;
import main.database.DatabaseConnectionPool;
import main.facade.ShoppingFacadeImpl;

public class ApplicationInitializer {
    private final ApplicationContext context;
    public DatabaseConnectionPool connectionPool;

    public ApplicationInitializer(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        // Obtain a connection from the pool
        Connection connection = context.connectionPool.getConnection();

        try {
            // Initialize the ReshelvingEventLogger with the connection
            ReshelvingEventLogger reshelvingEventLogger = new DatabaseReshelvingEventLogger(connection);

            // Use the logger in the ReplenishShelves implementation
            context.replenishShelves = new ReplenishShelvesImpl(context.stockDAO, context.shelvesDAO,
                    reshelvingEventLogger, LOW_SHELF_THRESHOLD, REPLENISHMENT_AMOUNT, REORDER_THRESHOLD);

            // Initialize the stock observer
            context.stockObserver = new StockReplenishmentObserver(context.replenishShelves, LOW_SHELF_THRESHOLD);
            context.stockDAO.addObserver(context.stockObserver);

            // Schedule expiration check task
            ExpirationCheckTask expirationCheckTask = new ExpirationCheckTask(context.stockDAO, context.shelvesDAO);
            context.expirationCheckScheduler = new ExpirationCheckScheduler(context.executorService);
            context.expirationCheckScheduler.scheduleDailyExpirationCheck(expirationCheckTask, EXPIRATION_CHECK_HOUR,
                    EXPIRATION_CHECK_MINUTE);

            // Start daily stock check
            DailyStockChecker dailyStockChecker = new DailyStockChecker(context.stockDAO, LOW_STOCK_THRESHOLD, context.executorService);
            dailyStockChecker.startDailyCheck(STOCK_CHECK_HOUR, STOCK_CHECK_MINUTE);

            // Schedule end-of-day replenishment
            context.endOfDayReplenisher = new EndOfDayShelfReplenisher(context.replenishShelves, context.executorService);
            context.endOfDayReplenisher.scheduleEndOfDayReplenishment(REPLENISHMENT_HOUR, REPLENISHMENT_MINUTE);

            // Initialize services
            CustomerService customerService = new CustomerServiceImpl(context.customerDAO);
            context.customerService = customerService;

            BillingService billingService = new BillingServiceImpl(context.billFactory, context.billDAO,
                    context.stockDAO,
                    context.shelvesDAO, context.replenishShelves, context.itemDAO, context.transactionFactory,
                    context.customerService);

            ReportService reportService = new ReportServiceImpl(context.reportFactory);

            // Initialize the facade with the services
            ShoppingFacadeImpl shoppingFacade = new ShoppingFacadeImpl(billingService, reportService, customerService);
            context.billingFacade = shoppingFacade;
            context.reportFacade = shoppingFacade;
            context.customerManagementFacade = shoppingFacade;

        } finally {
            // Always release the connection back to the pool
            context.connectionPool.releaseConnection(connection);
        }
    }
}
