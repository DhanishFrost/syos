package main.domain.dao;

import main.domain.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {
    private final Connection connection;
    private static final String INSERT_CUSTOMER_SQL = "INSERT INTO Customer (name, email, phone_number, loyaltyPoints, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_CUSTOMER_BY_ID_SQL = "SELECT * FROM Customer WHERE id = ?";
    private static final String FIND_CUSTOMER_BY_PHONE_NUMBER_SQL = "SELECT * FROM Customer WHERE phone_number = ?";
    private static final String FIND_CUSTOMER_BY_EMAIL_SQL = "SELECT * FROM Customer WHERE email = ?";
    private static final String FIND_ALL_CUSTOMERS_SQL = "SELECT * FROM Customer";
    private static final String UPDATE_CUSTOMER_SQL = "UPDATE Customer SET name = ?, email = ?, phone_number = ?, loyaltyPoints = ?, version = version + 1 WHERE id = ? AND version = ?";
    private static final String DELETE_CUSTOMER_SQL = "DELETE FROM Customer WHERE id = ?";
    private static final String UPDATE_LOYALTY_POINTS_SQL = "UPDATE Customer SET loyaltyPoints = ? WHERE id = ?";

    public CustomerDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveCustomer(Customer customer) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_CUSTOMER_SQL)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setInt(4, customer.getLoyaltyPoints());
            stmt.setTimestamp(5, customer.getCreatedAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Failed to save customer", e);
        }
    }

    @Override
    public Customer findCustomerById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_CUSTOMER_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to find customer by ID", e);
        }
        return null;
    }

    @Override
    public Customer findCustomerByPhoneNumber(String phoneNumber) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_CUSTOMER_BY_PHONE_NUMBER_SQL)) {
            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to find customer by phone number", e);
        }
        return null;
    }

    @Override
    public Customer findCustomerByEmail(String email) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_CUSTOMER_BY_EMAIL_SQL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to find customer by email", e);
        }
        return null;
    }

    @Override
    public List<Customer> findAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(FIND_ALL_CUSTOMERS_SQL)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to find all customers", e);
        }
        return customers;
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_CUSTOMER_SQL)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setInt(4, customer.getLoyaltyPoints());
            stmt.setInt(5, customer.getId());
            stmt.setInt(6, customer.getVersion());
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to update customer", e);
        }
    }

    @Override
    public void deleteCustomer(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_CUSTOMER_SQL)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Failed to delete customer", e);
        }
    }

    @Override
    public void updateLoyaltyPoints(int id, int loyaltyPoints) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_LOYALTY_POINTS_SQL)) {
            stmt.setInt(1, loyaltyPoints);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Failed to update loyalty points", e);
        }
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
        customer.setCreatedAt(rs.getTimestamp("created_at"));
        customer.setVersion(rs.getInt("version"));
        return customer;
    }

    public static class DAOException extends RuntimeException {
        public DAOException(String message) {
            super(message);
        }

        public DAOException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
