package main.service.customer;

import java.util.List;
import java.util.Map;

import main.domain.model.Customer;

public interface CustomerService {
    String addCustomer(Map<String, String[]> parameters);
    String editCustomer(Map<String, String[]> parameters);
    String deleteCustomer(Map<String, String[]> parameters);
    List<Customer> getAllCustomers();
    Customer findCustomerByPhoneNumber(String phoneNumber);
    void addLoyaltyPoints(int customerId, double purchaseAmount);
    void useLoyaltyPoints(int customerId, int loyaltyPoints);
    int findLoyaltyPointsByCustomerId(int customerId);
    Customer findCustomerById(int customerId);
}
