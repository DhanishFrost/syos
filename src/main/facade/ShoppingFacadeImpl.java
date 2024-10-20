package main.facade;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import main.domain.model.Bill;
import main.domain.model.Customer;
import main.domain.model.Transaction;
import main.service.billing.BillingService;
import main.service.customer.CustomerService;
import main.service.report.ReportService;

public class ShoppingFacadeImpl implements BillingFacade, ReportFacade, CustomerManagementFacade {
    private final BillingService billingService;
    private final ReportService reportService;
    private final CustomerService customerService;

    public ShoppingFacadeImpl(BillingService billingService, ReportService reportService,
            CustomerService customerService) {
        this.billingService = billingService;
        this.reportService = reportService;
        this.customerService = customerService;
    }

    // Billing Handling Method
    @Override
    public void handleBilling(String operation, Map<String, String[]> parameters, HttpServletRequest req,
            String billingToken) {
        HttpSession session = req.getSession();
        String resultMessage = null;

        switch (operation) {
            case "enterPhoneNumber":
                String customerPhone = billingService.handlePhoneNumber(parameters, billingToken);
                if ("skip".equals(customerPhone)) {
                    session.setAttribute("skipPhone", true); // Handle skip logic
                } else if (customerPhone != null && !customerPhone.equals("Invalid phone number.")) {
                    Customer customer = billingService.getCustomerByPhoneNumber(customerPhone);
                    if (customer != null) {
                        session.setAttribute("customerPhone", customerPhone);
                        session.setAttribute("customerLoyaltyPoints", customer.getLoyaltyPoints());
                        session.setAttribute("customerName", customer.getName()); // Store customer name
                    } else {
                        session.setAttribute("errorMessage", "Customer not found for the provided phone number.");
                    }
                } else {
                    session.setAttribute("errorMessage", customerPhone);
                }
                break;

            case "addItem":
                resultMessage = billingService.handleAddItem(parameters, billingToken);
                List<Transaction> addedItems = billingService.getCurrentTransactions(billingToken);
                session.setAttribute("addedItems", addedItems);

                if (!"Items added successfully".equals(resultMessage)) {
                    session.setAttribute("errorMessage", resultMessage);
                } else {
                    session.removeAttribute("errorMessage");
                }
                break;

            case "removeItem":
                resultMessage = billingService.handleRemoveItem(parameters, billingToken);
                addedItems = billingService.getCurrentTransactions(billingToken);
                session.removeAttribute("addedItemsDone");
                session.removeAttribute("discountsApplied");
                session.removeAttribute("discountRate");
                session.removeAttribute("loyaltyPoints");
                session.removeAttribute("finalAmount");
                session.removeAttribute("cashReceived");
                session.removeAttribute("changeAmount");
                session.removeAttribute("bill");

                if (addedItems == null || addedItems.isEmpty()) {
                    session.removeAttribute("addedItems"); // Remove from session if empty
                    session.removeAttribute("addedItemsDone");
                    System.out.println("All items removed from the session.");
                } else {
                    session.setAttribute("addedItems", addedItems);
                    System.out.println("Items remaining: " + addedItems.size());
                }

                if (session.getAttribute("addedItems") == null) {
                    System.out.println("addedItems successfully removed from session.");
                } else {
                    System.out.println("addedItems still in session: " + session.getAttribute("addedItems"));
                }

                break;

            case "doneAddingItems":
                resultMessage = billingService.handleDoneAddingItems(billingToken);
                session.setAttribute("addedItemsDone", true);
                break;

            case "applyDiscountAndLoyaltyPoints":
                resultMessage = billingService.handleApplyDiscountsAndLoyaltyPoints(parameters, billingToken);

                // Retrieve the final amount after applying discounts and loyalty points
                double finalAmount = billingService.getFinalAmount(billingToken);

                // If discounts and loyalty points were applied successfully
                if ("Discounts and loyalty points applied successfully".equals(resultMessage) && finalAmount > 0) {
                    session.setAttribute("discountsApplied", true); // Proceed to the next step
                    session.setAttribute("finalAmount", finalAmount);
                    session.setAttribute("resultMessage", resultMessage);
                    // Check if discountRate and loyaltyPoints are present in parameters
                    if (parameters.get("discountRate") != null) {
                        session.setAttribute("discountRate", parameters.get("discountRate")[0]);
                    }
                    if (parameters.get("loyaltyPoints") != null) {
                        session.setAttribute("loyaltyPoints", parameters.get("loyaltyPoints")[0]);
                    }

                    session.removeAttribute("errorMessage");
                } else {
                    session.setAttribute("errorMessage", resultMessage);
                    session.removeAttribute("discountsApplied");
                    session.removeAttribute("resultMessage");
                }
                break;

            case "calculateChange":
                // Call the billing service to handle the calculation of change
                resultMessage = billingService.handleCalculateChange(parameters, billingToken);

                double finalAmountAfterChange = billingService.getFinalAmount(billingToken);
                double changeAmount = billingService.getChangeAmount(billingToken);

                if (resultMessage.equals("Change calculated successfully.") && finalAmountAfterChange > 0) {
                    Bill bill = billingService.getBill(billingToken);

                    if (parameters.get("cashReceived") != null) {
                        session.setAttribute("cashReceived", parameters.get("cashReceived")[0]);
                    }
                    session.setAttribute("finalAmount", finalAmountAfterChange);
                    session.setAttribute("changeAmount", changeAmount);
                    session.setAttribute("successMessage", "Bill generated successfully.");
                    session.setAttribute("bill", bill);
                } else {
                    // If there was an error, store the error message in the session
                    session.setAttribute("errorMessage",
                            resultMessage != null ? resultMessage : "Error calculating the final amount.");
                }
                break;

            case "finalizeTransaction":
                resultMessage = billingService.handleFinalizeTransaction(billingToken);
                // Only clear session attributes if the transaction was successful
                if ("Transaction successfully completed".equals(resultMessage)) {
                    session.setAttribute("successMessage", "Billing successfully completed!");
                    session.removeAttribute("customerPhone");
                    session.removeAttribute("customerName");
                    session.removeAttribute("skipPhone");
                    session.removeAttribute("addedItems");
                    session.removeAttribute("discountsApplied");
                    session.removeAttribute("discountRate");
                    session.removeAttribute("loyaltyPoints");
                    session.removeAttribute("finalAmount");
                    session.removeAttribute("cashReceived");
                    session.removeAttribute("changeAmount");
                    session.removeAttribute("addedItemsDone");
                    session.removeAttribute("customerLoyaltyPoints");
                    session.removeAttribute("resultMessage");
                    session.removeAttribute("errorMessage");

                    billingService.resetBillingState(billingToken);
                } else if ("No current bill to finalize".equals(resultMessage)) {
                    session.removeAttribute("errorMessage");
                }  else {
                    session.setAttribute("errorMessage", resultMessage);
                }
                break;

            case "exitBilling":
                if (session != null) {
                    session.removeAttribute("customerPhone");
                    session.removeAttribute("skipPhone");
                    session.removeAttribute("addedItems");
                    session.removeAttribute("discountsApplied");
                    session.removeAttribute("finalAmount");
                    session.removeAttribute("changeAmount");
                    session.removeAttribute("addedItemsDone");
                    session.removeAttribute("customerLoyaltyPoints");
                    session.removeAttribute("resultMessage");
                    session.removeAttribute("successMessage");
                    session.removeAttribute("errorMessage");

                    billingService.resetBillingState(billingToken);

                    session.invalidate();
                }
                break;

            default:
                resultMessage = "Invalid operation.";
                break;
        }

        // Set result messages in the session (if needed)
        if (resultMessage != null) {
            session.setAttribute("resultMessage", resultMessage);
        }
    }

    // Report Generation Method
    @Override
    public String handleReportGeneration(Map<String, String[]> parameters) {
        String reportData = reportService.handleReportGeneration(parameters);
        // Return the report data
        return reportData != null ? reportData : "No report generated.";
    }

    // Customer Management Method
    @Override
    public List<Customer> manageCustomers(Map<String, String[]> parameters, int customerAction,
            HttpServletRequest req) {
        System.out.println("manageCustomers called with customerAction: " + customerAction);
        String message = null;
        String successMessage = null;
        List<Customer> customers = null;

        switch (customerAction) {
            case 1:
                customers = viewAllOrSpecificCustomerDetails(parameters);
                break;
            case 2:
                message = addCustomer(parameters);
                if (message == null) {
                    successMessage = "Customer added successfully.";
                }
                break;
            case 3:
                message = editCustomer(parameters);
                if (message == null) {
                    successMessage = "Customer details updated successfully.";
                }
                break;
            case 4:
                message = deleteCustomer(parameters);
                if (message == null) {
                    successMessage = "Customer deleted successfully.";
                }
                break;
            default:
                message = "Invalid customer action. Please enter a valid action.";
                break;
        }

        // Debugging output for messages
        System.out.println("Message: " + message);
        System.out.println("SuccessMessage: " + successMessage);

        // Reload customer list
        customers = getAllCustomers();

        // Set messages in the session
        HttpSession session = req.getSession();
        if (message != null) {
            session.setAttribute("errorMessage", message);
        } else if (successMessage != null) {
            session.setAttribute("successMessage", successMessage);
        }

        return customers;
    }

    @Override
    public String addCustomer(Map<String, String[]> parameters) {
        return customerService.addCustomer(parameters);
    }

    @Override
    public String editCustomer(Map<String, String[]> parameters) {
        return customerService.editCustomer(parameters);
    }

    @Override
    public String deleteCustomer(Map<String, String[]> parameters) {
        return customerService.deleteCustomer(parameters);
    }

    @Override
    public List<Customer> viewAllOrSpecificCustomerDetails(Map<String, String[]> parameters) {
        String[] inputArr = parameters.get("customerPhone");
        String input = (inputArr != null && inputArr.length > 0) ? inputArr[0].trim() : "";

        if (input.isEmpty()) {
            return customerService.getAllCustomers();
        } else {
            Customer customer = customerService.findCustomerByPhoneNumber(input);
            return customer != null ? List.of(customer) : List.of(); // return empty list if customer not found
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @Override
    public Customer findCustomerByPhoneNumber(String phoneNumber) {
        return customerService.findCustomerByPhoneNumber(phoneNumber);
    }
}
