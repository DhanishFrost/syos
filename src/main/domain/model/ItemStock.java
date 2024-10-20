package main.domain.model;

import java.sql.Date;

public class ItemStock {
    private int itemStockID;
    private String code;
    private String name;
    private int quantity;
    private Date dateOfPurchase;
    private Date expiryDate;

    public ItemStock(int itemStockID, String code, String name, int quantity, Date dateOfPurchase, Date expiryDate) {
        this.itemStockID = itemStockID;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.dateOfPurchase = dateOfPurchase;
        this.expiryDate = expiryDate;
    }

    // New constructor for findStockBelowReorderLevel
    public ItemStock(String itemCode, String itemName, int quantity) {
        this.code = itemCode;
        this.name = itemName;
        this.quantity = quantity;
    }

    // Getters and Setters

    public int getItemStockID() {
        return itemStockID;
    }

    public void setItemStockID(int itemStockID) {
        this.itemStockID = itemStockID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getStockDateOfPurchase() {
        return dateOfPurchase;
    }

    public void setStockDateOfPurchase(Date dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    public Date getStockExpiryDate() {
        return expiryDate;
    }

    public void setStockExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
