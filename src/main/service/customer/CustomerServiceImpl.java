package main.service.customer;

import main.domain.dao.CustomerDAO;
import main.domain.model.Customer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class CustomerServiceImpl implements CustomerService {
    private final CustomerDAO customerDAO;
    private final Lock customerServiceLock = new ReentrantLock(); // ReentrantLock for thread safety
    private static final long TIMEOUT_DURATION = 2; // 2 Seconds

    public CustomerServiceImpl(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    @Override
    public String addCustomer(Map<String, String[]> parameters) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    StringBuilder errorMessages = new StringBuilder();

                    String name = promptForValidInput(parameters, "customerName",
                            "Customer name cannot be blank. Please enter a valid name.",
                            input -> !input.trim().isEmpty());
                    if (name == null) {
                        errorMessages.append("Customer name cannot be blank.<br>");
                    }

                    String email = promptForValidInput(parameters, "customerEmail",
                            "Invalid email format. Please enter a valid email.",
                            this::isValidEmail);
                    if (email == null) {
                        errorMessages.append("Invalid email format.<br>");
                    } else if (customerDAO.findCustomerByEmail(email) != null) {
                        errorMessages.append("Email already exists: ").append(email).append("<br>");
                    }

                    String phoneNumber = promptForValidInput(parameters, "customerPhone",
                            "Invalid phone number. Please enter a 10-digit phone number.",
                            input -> input.length() == 10 && input.matches("\\d+"));
                    if (phoneNumber == null) {
                        errorMessages.append("Invalid phone number. Please enter a 10-digit phone number.<br>");
                    } else if (customerDAO.findCustomerByPhoneNumber(phoneNumber) != null) {
                        errorMessages.append("Phone number already exists: ").append(phoneNumber).append("<br>");
                    }

                    if (errorMessages.length() > 0) {
                        return errorMessages.toString();
                    }

                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setEmail(email);
                    customer.setPhoneNumber(phoneNumber);
                    customerDAO.saveCustomer(customer);
                    return null;
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                return "Could not process the request in time, please try again.";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Could not process the request, please try again.";
        }
    }

    private String promptForValidInput(Map<String, String[]> parameters, String paramName, String errorMessage,
            Predicate<String> validation) {
        String[] values = parameters.get(paramName);
        String input = (values != null && values.length > 0) ? values[0] : "";

        if (validation.test(input)) {
            return input;
        } else {
            return null;
        }
    }

    @Override
    public String editCustomer(Map<String, String[]> parameters) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    StringBuilder errorMessages = new StringBuilder();

                    String idStr = getParameterValue(parameters, "customerId");
                    if (idStr.isEmpty()) {
                        return "Customer ID is required.";
                    }

                    int customerId;
                    try {
                        customerId = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        return "Invalid Customer ID.";
                    }

                    Customer customer = customerDAO.findCustomerById(customerId);
                    if (customer == null) {
                        return "Customer not found.";
                    }

                    String versionStr = getParameterValue(parameters, "version");
                    if (versionStr.isEmpty()) {
                        return "Version is required for optimistic locking.";
                    }

                    int version;
                    try {
                        version = Integer.parseInt(versionStr);
                    } catch (NumberFormatException e) {
                        return "Invalid version format.";
                    }

                    // Check if the version in the request matches the version in the database
                    if (customer.getVersion() != version) {
                        return "Another user has already modified this customer. Please try again.";
                    }

                    // Process input fields for updating the customer
                    String name = getParameterValue(parameters, "customerName");
                    if (!name.isEmpty()) {
                        customer.setName(name);
                    }

                    String newEmail = getParameterValue(parameters, "customerEmail");
                    if (!newEmail.isEmpty() && !newEmail.equals(customer.getEmail())) {
                        if (isValidEmail(newEmail)) {
                            Customer existingCustomerByEmail = customerDAO.findCustomerByEmail(newEmail);
                            if (existingCustomerByEmail == null
                                    || existingCustomerByEmail.getId() == customer.getId()) {
                                customer.setEmail(newEmail);
                            } else {
                                errorMessages.append("Email already exists: ").append(newEmail).append("<br>");
                            }
                        } else {
                            errorMessages.append("Invalid email format.<br>");
                        }
                    }

                    String newPhoneNumber = getParameterValue(parameters, "customerPhone");
                    if (!newPhoneNumber.isEmpty() && !newPhoneNumber.equals(customer.getPhoneNumber())) {
                        if (newPhoneNumber.length() == 10 && newPhoneNumber.matches("\\d+")) {
                            Customer existingCustomerByPhone = customerDAO.findCustomerByPhoneNumber(newPhoneNumber);
                            if (existingCustomerByPhone == null
                                    || existingCustomerByPhone.getId() == customer.getId()) {
                                customer.setPhoneNumber(newPhoneNumber);
                            } else {
                                errorMessages.append("Phone number already exists: ").append(newPhoneNumber)
                                        .append("<br>");
                            }
                        } else {
                            errorMessages.append("Invalid phone number.<br>");
                        }
                    }

                    String loyaltyPointsStr = getParameterValue(parameters, "customerLoyaltyPoints");
                    if (!loyaltyPointsStr.isEmpty()) {
                        try {
                            int loyaltyPoints = Integer.parseInt(loyaltyPointsStr);
                            customer.setLoyaltyPoints(loyaltyPoints);
                        } catch (NumberFormatException e) {
                            errorMessages.append("Invalid value for loyalty points.<br>");
                        }
                    }

                    if (errorMessages.length() > 0) {
                        return errorMessages.toString();
                    }

                    // Update the customer data
                    boolean updateSuccess = customerDAO.updateCustomer(customer);
                    if (!updateSuccess) {
                        return "The customer data was modified by another user. Please try again.";
                    }

                    return null;
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                return "Could not process the request in time, please try again.";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Could not process the request, please try again.";
        }
    }

    private String getParameterValue(Map<String, String[]> parameters, String key) {
        String[] values = parameters.get(key);
        return (values != null && values.length > 0) ? values[0] : "";
    }

    @Override
    public String deleteCustomer(Map<String, String[]> parameters) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    String idStr = getParameterValue(parameters, "customerId");
                    if (idStr.isEmpty()) {
                        return "Customer ID is required.";
                    }

                    int customerId;
                    try {
                        customerId = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        return "Invalid Customer ID.";
                    }

                    Customer customer = customerDAO.findCustomerById(customerId);
                    if (customer == null) {
                        return "Customer not found.";
                    }

                    customerDAO.deleteCustomer(customerId);
                    return null;
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                return "Could not process the request in time, please try again.";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Could not process the request, please try again.";
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    return customerDAO.findAllCustomers();
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                throw new RuntimeException("Could not process the request in time, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not process the request, please try again.");
        }
    }

    @Override
    public Customer findCustomerByPhoneNumber(String phoneNumber) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    return customerDAO.findCustomerByPhoneNumber(phoneNumber);
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                throw new RuntimeException("Could not process the request in time, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not process the request, please try again.");
        }
    }

    @Override
    public Customer findCustomerById(int customerId) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    return customerDAO.findCustomerById(customerId);
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                throw new RuntimeException("Could not process the request in time, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not process the request, please try again.");
        }
    }

    @Override
    public void addLoyaltyPoints(int customerId, double purchaseAmount) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    int additionalPoints = (int) (purchaseAmount / 50);
                    Customer customer = customerDAO.findCustomerById(customerId);
                    if (customer != null) {
                        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + additionalPoints);
                        customerDAO.updateCustomer(customer);
                    }
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                throw new RuntimeException("Could not process the request in time, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not process the request, please try again.");
        }
    }

    @Override
    public void useLoyaltyPoints(int customerId, int pointsToUse) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    Customer customer = customerDAO.findCustomerById(customerId);
                    if (customer != null) {
                        int updatedPoints = customer.getLoyaltyPoints() - pointsToUse;
                        if (updatedPoints < 0) {
                            updatedPoints = 0;
                        }
                        customer.setLoyaltyPoints(updatedPoints);
                        customerDAO.updateCustomer(customer);
                    }
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                throw new RuntimeException("Could not process the request in time, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not process the request, please try again.");
        }
    }

    @Override
    public int findLoyaltyPointsByCustomerId(int customerId) {
        try {
            if (customerServiceLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    Customer customer = customerDAO.findCustomerById(customerId);
                    return customer != null ? customer.getLoyaltyPoints() : 0;
                } finally {
                    customerServiceLock.unlock();
                }
            } else {
                throw new RuntimeException("Could not process the request in time, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Could not process the request, please try again.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
