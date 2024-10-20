package main.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseReshelvingEventLogger implements ReshelvingEventLogger {
    private static final String INSERT_EVENT_SQL = "INSERT INTO ReshelvingEvents (ItemCode, Quantity, ReshelvingTimestamp) VALUES (?, ?, CURRENT_TIMESTAMP)";
    private Connection connection;

    public DatabaseReshelvingEventLogger(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void logReshelvingEvent(String itemCode, int quantity) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_EVENT_SQL)) {
            stmt.setString(1, itemCode);
            stmt.setInt(2, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
}
