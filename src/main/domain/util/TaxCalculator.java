package main.domain.util;

public class TaxCalculator {
    private double taxRate;

    public TaxCalculator(double taxRate) {
        this.taxRate = taxRate;
    }

    public double calculateTax(double amount) {
        return amount * (1 + taxRate / 100);
    }
}
