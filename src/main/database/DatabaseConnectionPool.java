package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class DatabaseConnectionPool {
    private final String url = "jdbc:mysql://localhost:3306/syos";
    private final String user = "root";
    private final String password = "";
    private final int MAX_POOL_SIZE = 10;
    private final Queue<Connection> connectionPool;

    public DatabaseConnectionPool() {
        connectionPool = new LinkedList<>();
        initializeConnectionPool();
    }

    private void initializeConnectionPool() {
        while (connectionPool.size() < MAX_POOL_SIZE) {
            connectionPool.add(createNewConnectionForPool());
        }
    }

    private Connection createNewConnectionForPool() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    public synchronized Connection getConnection() {
        if (connectionPool.isEmpty()) {
            // If the pool is empty, create a new connection
            return createNewConnectionForPool();
        } else {
            // Otherwise, return an available connection from the pool
            return connectionPool.poll();
        }
    }

    public synchronized void releaseConnection(Connection connection) {
        if (connection != null) {
            // Return the connection back to the pool
            connectionPool.offer(connection);
        }
    }

    public synchronized void shutdown() {
        while (!connectionPool.isEmpty()) {
            try {
                connectionPool.poll().close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing a connection in the pool", e);
            }
        }
    }
}
