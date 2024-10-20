package main.service.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.domain.dao.*;
import main.domain.model.*;
import main.scheduler.ReplenishShelves;
import main.service.customer.CustomerService;
import main.factory.*;
import main.command.*;
import main.decorators.TaxDecorator;
import main.decorators.LoyaltyPointsDecorator;
import main.strategies.discount.PercentageDiscountStrategy;
import main.strategies.payment.CashPaymentStrategyImpl;
import main.strategies.payment.PaymentStrategy;
import main.iterator.*;

public class BillingServiceImpl implements BillingService {
    private final BillFactory billFactory;
    private final BillDAO billDAO;
    private final StockDAO stockDAO;
    private final ShelvesDAO shelvesDAO;
    private final ReplenishShelves replenishShelves;
    private final ItemDAO itemDAO;
    private final TransactionFactory transactionFactory;
    private final CustomerService customerService;
    private final Lock billingLock = new ReentrantLock(); // Ensure thread safety
    private static final long TIMEOUT_DURATION = 2; // 2 Seconds

    private Map<String, Bill> billingSessions = new HashMap<>();
    private Map<String, List<Transaction>> transactionsMap = new HashMap<>();

    public BillingServiceImpl(BillFactory billFactory, BillDAO billDAO, StockDAO stockDAO, ShelvesDAO shelvesDAO,
            ReplenishShelves replenishShelves, ItemDAO itemDAO, TransactionFactory transactionFactory,
            CustomerService customerService) {
        this.billFactory = billFactory;
        this.billDAO = billDAO;
        this.stockDAO = stockDAO;
        this.shelvesDAO = shelvesDAO;
        this.replenishShelves = replenishShelves;
        this.itemDAO = itemDAO;
        this.transactionFactory = transactionFactory;
        this.customerService = customerService;
    }

    @Override
    public String handlePhoneNumber(Map<String, String[]> parameters, String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    // Check if skipPhone was selected
                    String[] skipPhoneArr = parameters.get("skipPhone");
                    if (skipPhoneArr != null && "true".equals(skipPhoneArr[0])) {
                        billingSessions.put(billingToken, createBill(null));
                        return "skip";
                    }

                    // Otherwise, process phone number normally
                    Customer customer = findCustomerFromRequest(parameters);
                    if (customer != null) {
                        billingSessions.put(billingToken, createBill(customer));
                        return customer.getPhoneNumber();
                    } else {
                        return "Invalid phone number.";
                    }
                } finally {
                    billingLock.unlock();
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
    public String handleAddItem(Map<String, String[]> parameters, String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    List<Transaction> transactions = transactionsMap.getOrDefault(billingToken, new ArrayList<>());
                    String errorMessage = validateAndAddItems(parameters, transactions);
                    transactionsMap.put(billingToken, transactions);
                    if (errorMessage == null) {
                        return "Items added successfully";
                    } else {
                        return errorMessage;
                    }
                } finally {
                    billingLock.unlock();
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
    public String handleRemoveItem(Map<String, String[]> parameters, String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    String[] itemCodeArr = parameters.get("itemCode");
                    String[] transactionIndexArr = parameters.get("transactionIndex");

                    List<Transaction> transactions = transactionsMap.getOrDefault(billingToken, new ArrayList<>());

                    if (itemCodeArr != null && itemCodeArr.length > 0 && transactionIndexArr != null
                            && transactionIndexArr.length > 0) {
                        String itemCodeToRemove = itemCodeArr[0];
                        int transactionIndex = Integer.parseInt(transactionIndexArr[0]);

                        // Ensure the transaction index is within range
                        if (transactionIndex < 0 || transactionIndex >= transactions.size()) {
                            return "Error removing item: Invalid transaction index.";
                        }

                        Transaction transaction = transactions.get(transactionIndex);
                        List<Transaction.TransactionItem> modifiableItems = transaction.getItems();

                        boolean itemRemoved = false;

                        // Find the specific item to remove in the transaction
                        for (Transaction.TransactionItem transactionItem : modifiableItems) {
                            if (transactionItem.getItem().getCode().equals(itemCodeToRemove)) {
                                Command removeItemCmd = new RemoveItemCommand(transaction, transactionItem.getItem(),
                                        transactionItem.getQuantity());
                                removeItemCmd.execute();
                                itemRemoved = true;
                                break;
                            }
                        }

                        // If no item was removed, return an error message
                        if (!itemRemoved) {
                            return "Error removing item: Item not found.";
                        }

                        // Check if the transaction is empty and remove the transaction if necessary
                        if (transaction.getItems().isEmpty()) {
                            transactions.remove(transactionIndex);
                        }

                        transactionsMap.put(billingToken, transactions); // Update the transactions in the map

                        // After removal, check if there are any transactions left
                        if (transactions.isEmpty()) {
                            return "All items removed";
                        }

                        return "Item removed successfully";
                    }
                    return "Error removing item: No item code or transaction index provided.";
                } finally {
                    billingLock.unlock();
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
    public String handleDoneAddingItems(String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    Bill bill = billingSessions.get(billingToken);
                    List<Transaction> transactions = transactionsMap.getOrDefault(billingToken, new ArrayList<>());
                    if (bill != null) {
                        bill = processTransactionsWithoutStockDeduction(bill, transactions);
                        billingSessions.put(billingToken, bill); // Update the bill in the session
                        return "Done adding items";
                    }
                    return "No current bill available";
                } finally {
                    billingLock.unlock();
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
    public List<Transaction> getCurrentTransactions(String billingToken) {
        return new ArrayList<>(transactionsMap.getOrDefault(billingToken, new ArrayList<>()));
    }

    @Override
    public String handleApplyDiscountsAndLoyaltyPoints(Map<String, String[]> parameters, String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    Bill bill = billingSessions.get(billingToken);
                    if (bill != null) {
                        // Apply discounts
                        String discountError = applyDiscount(parameters, bill);

                        // Fetch the customer details using the customerId
                        int customerId = bill.getCustomerId();
                        Customer customer = customerService.findCustomerById(customerId);

                        // Apply loyalty points
                        String loyaltyError = applyLoyaltyPointsIfApplicable(parameters, customer, bill);

                        // Check for errors in applying discounts or loyalty points
                        if (discountError != null) {
                            return discountError;
                        }

                        if (loyaltyError != null) {
                            return loyaltyError;
                        }

                        // Recalculate the final amount after applying discounts and loyalty points
                        double finalAmount = bill.calculateFinalAmountWithTax();
                        bill.setFinalPrice(finalAmount);
                        billingSessions.put(billingToken, bill);

                        if (finalAmount > 0) {
                            return "Discounts and loyalty points applied successfully";
                        } else {
                            return "Final amount calculation error. Please check your inputs.";
                        }
                    }
                    return "No current bill available";
                } finally {
                    billingLock.unlock();
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
    public double getFinalAmount(String billingToken) {
        Bill bill = billingSessions.get(billingToken);
        if (bill != null) {
            return bill.getFinalPrice() - bill.getLoyaltyPointsUsed();
        }
        return 0.0;
    }

    @Override
    public String handleCalculateChange(Map<String, String[]> parameters, String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    Bill bill = billingSessions.get(billingToken);
                    if (bill != null) {
                        // Apply tax
                        BillComponent taxedBill = new TaxDecorator(bill, 5.0);

                        // Calculate final amount with tax
                        double finalAmount = taxedBill.calculateFinalAmount() - bill.getLoyaltyPointsUsed();
                        bill.setFinalPrice(finalAmount); // Set the final price in the Bill

                        // Get the cash received from parameters
                        String[] cashReceivedArr = parameters.get("cashReceived");
                        double cashReceived = 0;
                        if (cashReceivedArr != null && cashReceivedArr.length > 0) {
                            try {
                                cashReceived = Double.parseDouble(cashReceivedArr[0]);
                            } catch (NumberFormatException e) {
                                return "Invalid cash received amount. Please enter a valid number.";
                            }
                        }

                        if (cashReceived < finalAmount) {
                            return "Insufficient cash received. Please provide enough to cover the total amount.";
                        }

                        // Calculate the change
                        PaymentStrategy paymentStrategy = new CashPaymentStrategyImpl(cashReceived);
                        paymentStrategy.pay(finalAmount, bill);
                        billingSessions.put(billingToken, bill); // Update the bill in the session

                        return "Change calculated successfully.";
                    }
                    return "No current bill available.";
                } finally {
                    billingLock.unlock();
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
    public double getChangeAmount(String billingToken) {
        Bill bill = billingSessions.get(billingToken);
        return bill != null ? bill.getChangeGiven() : 0.0;
    }

    @Override
    public String handleFinalizeTransaction(String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    Bill bill = billingSessions.get(billingToken);
                    List<Transaction> transactions = transactionsMap.getOrDefault(billingToken, new ArrayList<>());

                    if (bill != null) {
                        // Perform stock recheck before finalizing (Optimistic Locking)
                        for (Transaction transaction : transactions) {
                            for (Transaction.TransactionItem transactionItem : transaction.getItems()) {
                                String itemCode = transactionItem.getItem().getCode();
                                int requestedQuantity = transactionItem.getQuantity();

                                // Check current stock levels
                                int availableQuantity = stockDAO.getTotalAvailableQuantity(itemCode);

                                // If the available quantity is less than the requested, reject the transaction
                                if (availableQuantity < requestedQuantity) {
                                    return "Stock conflict detected for item " + itemCode + ". Only "
                                            + availableQuantity + " items are available, but you requested "
                                            + requestedQuantity + ". Please adjust your order.";
                                }
                            }
                        }

                        // If no conflict, process the transactions and deduct the stock
                        processTransactionsWithStockDeduction(bill, transactions);

                        // Save the final bill to the database
                        billDAO.saveBill(bill);

                        // Reset billing state after finalizing
                        resetBillingState(billingToken);

                        return "Transaction successfully completed";
                    }
                    return "No current bill to finalize";
                } finally {
                    billingLock.unlock();
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
    public Bill getBill(String billingToken) {
        return billingSessions.get(billingToken);
    }

    @Override
    public void resetBillingState(String billingToken) {
        try {
            if (billingLock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    billingSessions.remove(billingToken);
                    transactionsMap.remove(billingToken);
                } finally {
                    billingLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Customer getCustomerByPhoneNumber(String phoneNumber) {
        return customerService.findCustomerByPhoneNumber(phoneNumber);
    }

    // Helper Methods
    private Customer findCustomerFromRequest(Map<String, String[]> parameters) {
        String phoneNumber = promptForValidInput(parameters, "customerPhone",
                "Invalid phone number format. Please enter a 10-digit phone number.",
                input -> input.length() == 10 && input.matches("\\d+"));
        return (phoneNumber != null) ? customerService.findCustomerByPhoneNumber(phoneNumber) : null;
    }

    private Bill createBill(Customer customer) {
        Bill bill = billFactory.createBill();
        if (customer != null) {
            bill.setCustomerId(customer.getId());
        }
        bill.setBillDate(new Date());
        return bill;
    }

    private String applyDiscount(Map<String, String[]> parameters, Bill bill) {
        String[] discountRateArr = parameters.get("discountRate");

        if (discountRateArr != null && discountRateArr.length > 0) {
            String discountRateStr = discountRateArr[0].trim();
            if (!discountRateStr.isEmpty()) {
                try {
                    double discountRate = Double.parseDouble(discountRateStr);
                    if (discountRate < 0 || discountRate > 100) {
                        return "Invalid discount rate. Please enter a value between 0 and 100.";
                    }
                    bill.setDiscountStrategy(new PercentageDiscountStrategy(discountRate));
                } catch (NumberFormatException e) {
                    return "Invalid discount input: '" + discountRateStr + "'. Please enter a valid number.";
                }
            }
        }
        return null;
    }

    private String applyLoyaltyPointsIfApplicable(Map<String, String[]> parameters, Customer customer, Bill bill) {
        if (customer != null && customer.getLoyaltyPoints() > 10) { // Loyalty points only available if > 10
            double totalPriceBeforeDiscount = bill.calculateFinalAmount();
            String[] pointsToUseArr = parameters.get("loyaltyPoints");

            int pointsToUse = 0;
            if (pointsToUseArr != null && pointsToUseArr.length > 0) {
                try {
                    pointsToUse = Integer.parseInt(pointsToUseArr[0]);
                } catch (NumberFormatException e) {
                    return "Invalid loyalty points input. Please enter a valid number.";
                }
            }

            // Check if points to use are greater than available or greater than the bill
            // total
            if (pointsToUse > 0 && pointsToUse <= Math.min(customer.getLoyaltyPoints(), totalPriceBeforeDiscount)) {
                BillComponent decoratedBill = new LoyaltyPointsDecorator(bill, pointsToUse);
                bill.setLoyaltyPointsUsed(pointsToUse);
                customerService.useLoyaltyPoints(customer.getId(), pointsToUse);
                double newFinalAmount = decoratedBill.calculateFinalAmount();
                bill.setFinalPrice(newFinalAmount);
            } else if (pointsToUse > customer.getLoyaltyPoints()) {
                return "You don't have enough loyalty points.";
            } else if (pointsToUse > totalPriceBeforeDiscount) {
                return "Loyalty points exceed the bill total.";
            }
        }
        return null;
    }

    private String validateAndAddItems(Map<String, String[]> parameters, List<Transaction> transactions) {
        Transaction currentTransaction = transactionFactory.createTransaction();
        StringBuilder errorMessage = new StringBuilder();

        String[] itemCodes = parameters.get("itemCode");
        String[] quantities = parameters.get("quantity");

        if (itemCodes != null && quantities != null) {
            Map<String, Integer> totalRequestedQuantities = new HashMap<>();

            // Calculate total quantities already added in previous transactions
            for (Transaction transaction : transactions) {
                for (Transaction.TransactionItem transactionItem : transaction.getItems()) {
                    String itemCode = transactionItem.getItem().getCode();
                    int currentQuantity = transactionItem.getQuantity();
                    totalRequestedQuantities.put(itemCode,
                            totalRequestedQuantities.getOrDefault(itemCode, 0) + currentQuantity);
                }
            }

            for (int i = 0; i < itemCodes.length; i++) {
                String itemCode = itemCodes[i];
                int quantity;

                try {
                    quantity = Integer.parseInt(quantities[i]);
                } catch (NumberFormatException e) {
                    errorMessage.append("Invalid quantity for item code: ").append(itemCode).append(". ");
                    continue; // Skip invalid quantity input
                }

                // Validate the item code and check availability
                Item item = itemDAO.findItemByCode(itemCode);
                if (item == null) {
                    errorMessage.append("Item code ").append(itemCode).append(" does not exist. ");
                } else {
                    int availableQuantity = stockDAO.getTotalAvailableQuantity(itemCode);

                    // Calculate the total requested quantity (including the current request)
                    int totalRequestedQuantity = totalRequestedQuantities.getOrDefault(itemCode, 0) + quantity;

                    // Check if the requested quantity exceeds the available stock
                    if (totalRequestedQuantity > availableQuantity) {
                        errorMessage.append("Requested quantity for item code ").append(itemCode)
                                .append(" exceeds available stock. Available quantity: ").append(availableQuantity)
                                .append(". ");
                    } else {
                        // Update the total requested quantities map
                        totalRequestedQuantities.put(itemCode, totalRequestedQuantity);

                        // Add the item to the current transaction
                        Command addItemCmd = new AddItemCommand(currentTransaction, item, quantity);
                        currentTransaction.executeCommand(addItemCmd);
                    }
                }
            }
        }

        if (!currentTransaction.getItems().isEmpty()) {
            transactions.add(currentTransaction); // Add the transaction to the list
        }

        return errorMessage.length() > 0 ? errorMessage.toString() : null;
    }

    private Bill processTransactionsWithoutStockDeduction(Bill bill, List<Transaction> transactions) {
        BillCollection billCollection = new BillCollection(transactions);
        Iterator<Transaction> transactionIterator = billCollection.createIterator();

        while (transactionIterator.hasNext()) {
            Transaction transaction = transactionIterator.next();
            bill.addTransaction(transaction);
        }
        return bill;
    }

    private void processTransactionsWithStockDeduction(Bill bill, List<Transaction> transactions) {
        BillCollection billCollection = new BillCollection(transactions);
        Iterator<Transaction> transactionIterator = billCollection.createIterator();

        while (transactionIterator.hasNext()) {
            Transaction transaction = transactionIterator.next();

            // Iterate through TransactionItem using TransactionIterator
            TransactionCollection itemCollection = new TransactionCollection(transaction.getItems());
            Iterator<Transaction.TransactionItem> itemIterator = itemCollection.createIterator();

            while (itemIterator.hasNext()) {
                Transaction.TransactionItem item = itemIterator.next();
                shelvesDAO.updateShelfQuantityAndRemoveIfZero(item.getItem().getCode(), item.getQuantity());
                replenishShelves.replenishShelves(item.getItem().getCode());
            }
        }
    }

    private String promptForValidInput(Map<String, String[]> parameters, String paramName, String errorMessage,
            java.util.function.Predicate<String> validation) {
        String[] values = parameters.get(paramName);
        String input = (values != null && values.length > 0) ? values[0].trim() : "";

        if (validation.test(input)) {
            return input;
        } else {
            return null;
        }
    }
}