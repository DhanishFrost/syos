package main.domain.model;

public class Item {
    private String code;
    private String name;
    private double price;

    public Item(String code, String name, double price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }
}