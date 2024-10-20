package main.facade;

import java.util.Map;
import java.util.List;
import main.domain.model.Customer;
import javax.servlet.http.HttpServletRequest;

public interface CustomerManagementFacade {
    List<Customer> manageCustomers(Map<String, String[]> parameters, int customerAction, HttpServletRequest req);
    String addCustomer(Map<String, String[]> parameters);
    String editCustomer(Map<String, String[]> parameters);
    String deleteCustomer(Map<String, String[]> parameters);
    List<Customer> viewAllOrSpecificCustomerDetails(Map<String, String[]> parameters);
    List<Customer> getAllCustomers();
    Customer findCustomerByPhoneNumber(String phoneNumber);
}