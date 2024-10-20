package main.domain.model;

import java.sql.Date;

public class Shelf {
    private int shelvesID; // This is the primary key
    private String code;
    private int quantity;
    private Date movedDate;
    private Date expiryDate;

    public Shelf(String code, int quantity, Date movedDate, Date expiryDate) {
        this.code = code;
        this.quantity = quantity;
        this.movedDate = movedDate;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public int getShelvesID() {
        return shelvesID;
    }

    public void setShelvesID(int shelvesID) {
        this.shelvesID = shelvesID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getMovedDate() {
        return movedDate;
    }

    public void setMovedDate(Date movedDate) {
        this.movedDate = movedDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
