package main.domain.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.domain.model.Item;

public class ItemDAOImpl implements ItemDAO {
    private Connection connection;

    public ItemDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Item findItemByCode(String code) {
        String query = "SELECT * FROM items WHERE item_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Item(resultSet.getString("item_code"), resultSet.getString("item_name"), resultSet.getDouble("item_price"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding item by code", e);
        }
        return null;
    }
}
