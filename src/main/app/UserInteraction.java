package main.app;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import main.domain.model.Customer;

public class UserInteraction {
    private final ApplicationContext context;

    public UserInteraction(ApplicationContext context) {
        this.context = context;
    }

    public void processRequest(Map<String, String[]> parameters, HttpServletRequest req, String billingToken) {
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();
            
            System.out.println("Parameter Name: " + paramName);
            for (String value : paramValues) {
                System.out.println("Value: " + value);
            }
        }

        // Get the action parameter to determine what the user wants to do
        String[] choiceArr = parameters.get("action");
        if (choiceArr == null || choiceArr.length == 0) {
            setSessionMessage(req, "errorMessage", "No action specified. Please select a valid action.");
            return;
        }

        int choice;
        try {
            choice = Integer.parseInt(choiceArr[0]);
            System.out.println("Processing choice: " + choice);
        } catch (NumberFormatException e) {
            setSessionMessage(req, "errorMessage", "Invalid choice. Please enter a valid number.");
            return;
        }

        // Handle the action based on the user's choice
        switch (choice) {
            case 1:
                handleBilling(parameters, req, billingToken);
                break;
            case 2:
                handleReportGeneration(parameters, req);
                break;
            case 3:
                handleCustomerActions(parameters, req);
                break;
            case 4:
                exit();
                break;
            default:
                setSessionMessage(req, "errorMessage", "Invalid choice. Please enter a number between 1 and 4.");
        }
    }

    private void handleBilling(Map<String, String[]> parameters, HttpServletRequest req, String billingToken) {
        System.out.println("Handling billing...");
        String operation = parameters.get("operation")[0];

        if (operation != null) {
            context.billingFacade.handleBilling(operation, parameters, req, billingToken);
        } else {
            req.getSession().setAttribute("errorMessage", "No operation specified.");
        }

        // Retain form data if there's an error
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue()[0]);
        }
    }

    private void handleReportGeneration(Map<String, String[]> parameters, HttpServletRequest req) {
        System.out.println("Generating report...");
        String reportData = context.reportFacade.handleReportGeneration(parameters);
        if (reportData != null) {
            req.setAttribute("reportData", reportData);
            setSessionMessage(req, "successMessage", "Report generated successfully.");
        } else {
            setSessionMessage(req, "errorMessage", "No report generated.");
        }
    }

    private void handleCustomerActions(Map<String, String[]> parameters, HttpServletRequest req) {
        String[] customerActionArr = parameters.get("customerAction");
        int customerAction = 0;

        if (customerActionArr != null && customerActionArr.length > 0) {
            try {
                customerAction = Integer.parseInt(customerActionArr[0]);
                System.out.println("Processing customer action: " + customerAction);
            } catch (NumberFormatException e) {
                setSessionMessage(req, "errorMessage", "Invalid customer action.");
                return;
            }
        }

        if (customerAction != 0) {
            List<Customer> customers = context.customerManagementFacade.manageCustomers(parameters, customerAction,
                    req);
            if (customers != null) {
                req.setAttribute("customers", customers);
            } else {
                setSessionMessage(req, "errorMessage", "Customer action could not be completed.");
            }
        }
    }

    private void setSessionMessage(HttpServletRequest req, String messageType, String message) {
        HttpSession session = req.getSession();
        session.setAttribute(messageType, message);
    }

    private void exit() {
        System.out.println("Exiting...");
        context.shutdown();
    }
}
