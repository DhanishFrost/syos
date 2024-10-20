package main.domain.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import main.domain.model.Bill;
import main.domain.model.Transaction;
import main.domain.model.Transaction.TransactionItem;

public class BillDAOImpl implements BillDAO {
    private static final String INSERT_BILL_SQL_WITH_CUSTOMER = "INSERT INTO Bills (bill_date, total_price, discount_amount, tax_amount, loyalty_points_used, final_price, cash_tendered, change_amount, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_BILL_SQL_WITHOUT_CUSTOMER = "INSERT INTO Bills (bill_date, total_price, discount_amount, tax_amount, final_price, cash_tendered, change_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_TRANSACTION_SQL = "INSERT INTO Transactions (bill_id, item_id, quantity, total_price) VALUES (?, ?, ?, ?)";

    private Connection connection;

    public BillDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveBill(Bill bill) {
        try {
            connection.setAutoCommit(false); // Start transaction

            int billId = insertBill(bill);
            bill.setBillId(billId);
            saveTransactions(bill.getTransactions(), billId);

            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                e.printStackTrace();
            }
            System.err.println("Error saving bill: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int insertBill(Bill bill) throws SQLException {
        String sql = bill.getCustomerId() != 0 ? INSERT_BILL_SQL_WITH_CUSTOMER : INSERT_BILL_SQL_WITHOUT_CUSTOMER;

        try (PreparedStatement billStmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            billStmt.setDate(1, new Date(bill.getBillDate().getTime()));
            billStmt.setDouble(2, bill.getTotalAmount());
            billStmt.setDouble(3, bill.getTotalAmount() - bill.calculateFinalAmount());
            billStmt.setDouble(4, bill.calculateFinalAmountWithTax() - bill.calculateFinalAmount());

            if (bill.getCustomerId() != 0) {
                billStmt.setInt(5, bill.getLoyaltyPointsUsed());
                billStmt.setDouble(6, bill.getFinalPrice());
                billStmt.setDouble(7, bill.getCashTendered());
                billStmt.setDouble(8, bill.getChangeGiven());
                billStmt.setInt(9, bill.getCustomerId());
            } else {
                billStmt.setDouble(5, bill.getFinalPrice());
                billStmt.setDouble(6, bill.getCashTendered());
                billStmt.setDouble(7, bill.getChangeGiven());
            }

            billStmt.executeUpdate();

            try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Retrieve and return the generated bill ID
                } else {
                    throw new SQLException("Creating bill failed, no ID obtained.");
                }
            }
        }
    }

    private void saveTransactions(List<Transaction> transactions, int billId) throws SQLException {
        try (PreparedStatement transactionStmt = connection.prepareStatement(INSERT_TRANSACTION_SQL)) {
            for (Transaction transaction : transactions) {
                for (TransactionItem item : transaction.getItemQuantities()) {
                    transactionStmt.setInt(1, billId);
                    transactionStmt.setString(2, item.getItem().getCode());
                    transactionStmt.setInt(3, item.getQuantity());
                    transactionStmt.setDouble(4, item.getItem().getPrice() * item.getQuantity());
                    transactionStmt.executeUpdate();
                }
            }
        }
    }
}
