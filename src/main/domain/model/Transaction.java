package main.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import main.command.Command;

public class Transaction {
    private int id;
    private int billId;
    private String itemId;
    private String itemName;
    private int quantity;
    private double totalPrice;
    private List<TransactionItem> items;
    private Stack<Command> commandStack;
    private Stack<Command> redoStack;

    public Transaction() {
        this.items = new ArrayList<>();
        this.commandStack = new Stack<>();
        this.redoStack = new Stack<>();
    }
    
    // Method to clear all items in the transaction
    public void clear() {
        this.id = 0;
        this.billId = 0;
        this.itemId = null;
        this.itemName = null;
        this.quantity = 0;
        this.totalPrice = 0.0;
        this.items.clear();
        this.commandStack.clear();
        this.redoStack.clear();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<TransactionItem> getItemQuantities() {
        return new ArrayList<>(items);
    }
    

    public TransactionItem getQuantity(Item item) {
        for (TransactionItem iq : items) {
            if (iq.getItem().equals(item)) {
                return iq;
            }
        }
        return null;
    }

    public Stack<Command> getRedoStack() {
        return redoStack;
    }

    // Method to add an item to the transaction
    public void addItem(Item item, int quantity) {
        if (quantity <= 0) return;
        for (TransactionItem iq : items) {
            if (iq.item.equals(item)) {
                iq.quantity += quantity;
                iq.calculateTotalPrice();
                updateTotalPrice();
                return;
            }
        }
        TransactionItem newItem = new TransactionItem(item, quantity);
        items.add(newItem);
        updateTotalPrice();
    }

    // Method to remove an item from the transaction
    public void removeItem(Item item, int quantity) {
        TransactionItem toRemove = null;
        for (TransactionItem iq : items) {
            if (iq.item.equals(item)) {
                iq.quantity -= quantity;
                iq.calculateTotalPrice();
                if (iq.quantity <= 0) {
                    toRemove = iq;
                }
                break;
            }
        }
        if (toRemove != null) {
            items.remove(toRemove);
        }
        updateTotalPrice();
    }

    // Method to execute a command
    public void executeCommand(Command command) {
        command.execute();
        commandStack.push(command);
        redoStack.clear(); // Clear redo stack after executing a new command
    }

    // Method to undo the last command
    public void undoLastCommand() {
        if (!commandStack.isEmpty()) {
            Command lastCommand = commandStack.pop();
            lastCommand.undo();
            redoStack.push(lastCommand);
        }
    }

    // Method to redo the last undone command
    public void redoLastCommand() {
        if (!redoStack.isEmpty()) {
            Command lastUndoneCommand = redoStack.pop();
            lastUndoneCommand.execute();
            commandStack.push(lastUndoneCommand);
        }
    }

    // Update total price of all items in the transaction
    private void updateTotalPrice() {
        this.totalPrice = items.stream().mapToDouble(TransactionItem::getTotalPrice).sum();
    }

    // Public getter to retrieve all items with their quantities
    public List<TransactionItem> getItems() {
        return items;
    }

    // Inner class TransactionItem
    public static class TransactionItem {
        private Item item;
        private int quantity;
        private double totalPrice;

        TransactionItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
            calculateTotalPrice();
        }

        // Public getters for TransactionItem
        public Item getItem() {
            return item;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public int setQuantity(int quantity) {
            return this.quantity = quantity;
        }

        // Public setter for TotalPrice
        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public void calculateTotalPrice() {
            this.totalPrice = this.item.getPrice() * this.quantity;
        }
    }
}
