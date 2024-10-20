package main.service.billing;

import java.util.List;
import java.util.Map;

import main.domain.model.Customer;
import main.domain.model.Transaction;
import main.domain.model.Bill;

public interface BillingService {
    String handlePhoneNumber(Map<String, String[]> parameters, String billingToken);
    String handleAddItem(Map<String, String[]> parameters, String billingToken);
    String handleRemoveItem(Map<String, String[]> parameters, String billingToken);
    String handleDoneAddingItems(String billingToken);
    String handleApplyDiscountsAndLoyaltyPoints(Map<String, String[]> parameters, String billingToken);
    String handleCalculateChange(Map<String, String[]> parameters, String billingToken);
    String handleFinalizeTransaction(String billingToken);
    List<Transaction> getCurrentTransactions(String billingToken); 
    double getFinalAmount(String billingToken);
    double getChangeAmount(String billingToken);
    Customer getCustomerByPhoneNumber(String phoneNumber);
    void resetBillingState(String billingToken);
    Bill getBill(String billingToken);
}
