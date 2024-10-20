package main.strategies.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscountStrategy implements DiscountStrategy {
    private double discountPercentage;

    public PercentageDiscountStrategy(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double applyDiscount(double totalAmount) {
        double discountedAmount = totalAmount * (1 - discountPercentage / 100);

        // Round to one decimal place
        BigDecimal roundedAmount = new BigDecimal(discountedAmount).setScale(1, RoundingMode.HALF_UP);

        return roundedAmount.doubleValue();
    }
}
