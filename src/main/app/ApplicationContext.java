package main.app;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import main.database.DatabaseConnectionPool;
import main.domain.dao.*;
import main.scheduler.*;
import main.facade.*;
import main.factory.BillFactory;
import main.factory.TransactionFactory;
import main.objectPool.BillPool;
import main.objectPool.TransactionPool;
import main.factory.ReportFactory;
import main.observer.Observer;
import main.service.customer.CustomerService;

public class ApplicationContext {
    public DatabaseConnectionPool connectionPool;
    public ItemDAO itemDAO;
    public BillDAO billDAO;
    public StockDAO stockDAO;
    public ShelvesDAO shelvesDAO;
    public CustomerDAO customerDAO;
    public BillingFacade billingFacade;
    public ReportFacade reportFacade;
    public CustomerManagementFacade customerManagementFacade;
    public ReplenishShelves replenishShelves;
    public CustomerService customerService;

    // Factories
    public BillFactory billFactory;
    public TransactionFactory transactionFactory;
    public ReportFactory reportFactory;

    // Observer
    public Observer stockObserver;

    // Scheduler
    public ExpirationCheckScheduler expirationCheckScheduler;
    public EndOfDayShelfReplenisher endOfDayReplenisher;
    public DailyStockChecker dailyStockChecker;

    // Common ExecutorService for concurrency
    public final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public ApplicationContext() {
        // Initialize the connection pool
        connectionPool = new DatabaseConnectionPool();

        // Get a connection from the pool for initializing DAOs
        Connection connection = connectionPool.getConnection();

        try {
            // DAOs
            this.itemDAO = new ItemDAOImpl(connection);
            this.billDAO = new BillDAOImpl(connection);
            this.stockDAO = new StockDAOImpl(connection);
            this.shelvesDAO = new ShelvesDAOImpl(connection);
            this.customerDAO = new CustomerDAOImpl(connection);

            // Factories
            this.billFactory = new BillFactory(new BillPool());
            this.transactionFactory = new TransactionFactory(new TransactionPool());
            this.reportFactory = new ReportFactory(connection, stockDAO);

        } finally {
            // Release the connection back to the pool after initialization
            connectionPool.releaseConnection(connection);
        }

    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    // Shutdown the application
    public void shutdown() {
        // Release all database resources
        connectionPool.shutdown();

        // Shutdown executor service
        executorService.shutdownNow();
    }
}
