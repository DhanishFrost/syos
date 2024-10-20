package main.domain.util;

import main.strategies.discount.DiscountStrategy;

public class DiscountCalculator {
    private DiscountStrategy discountStrategy;

    public DiscountCalculator(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public double applyDiscount(double totalAmount) {
        return discountStrategy.applyDiscount(totalAmount);
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }
}
