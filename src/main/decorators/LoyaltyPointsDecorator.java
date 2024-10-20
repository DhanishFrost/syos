package main.decorators;

import main.domain.model.BillComponent;

public class LoyaltyPointsDecorator implements BillComponent {
    private BillComponent wrappedBill;
    private int pointsToUse;

    public LoyaltyPointsDecorator(BillComponent billComponent, int pointsToUse) {
        this.wrappedBill = billComponent;
        this.pointsToUse = pointsToUse;
    }

    @Override
    public double calculateFinalAmount() {
        double originalAmount = wrappedBill.calculateFinalAmount();
        double loyaltyPoints = pointsToUse * 1.0; 
        return originalAmount - loyaltyPoints;
    }

    public double getLoyaltyPointsDiscount() {
        return pointsToUse * 1.0; 
    }
}
