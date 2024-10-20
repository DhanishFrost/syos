package main.domain.dao;

import main.domain.model.Customer;
import java.util.List;

public interface CustomerDAO {
    void saveCustomer(Customer customer);
    Customer findCustomerById(int id);
    Customer findCustomerByPhoneNumber(String phoneNumber);
    Customer findCustomerByEmail(String email);
    List<Customer> findAllCustomers();
    boolean updateCustomer(Customer customer);
    void deleteCustomer(int id);
    void updateLoyaltyPoints(int id, int loyaltyPoints);
}
