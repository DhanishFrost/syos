package main.domain.model;

public class TransactionBuilder {
    private int id;
    private int billId;
    private String itemId;
    private String itemName;
    private int quantity;
    private double totalPrice;

    public TransactionBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public TransactionBuilder setBillId(int billId) {
        this.billId = billId;
        return this;
    }

    public TransactionBuilder setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public TransactionBuilder setItemName(String itemName) {
        this.itemName = itemName;
        return this;
    }

    public TransactionBuilder setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public TransactionBuilder setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public Transaction build() {
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setBillId(this.billId);
        transaction.setItemId(this.itemId);
        transaction.setItemName(this.itemName);
        transaction.setQuantity(this.quantity);
        transaction.setTotalPrice(this.totalPrice);
        return transaction;
    }
}
