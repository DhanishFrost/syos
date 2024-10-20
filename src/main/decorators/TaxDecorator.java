package main.decorators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import main.domain.model.BillComponent;

public class TaxDecorator implements BillComponent {
    private BillComponent wrappedBill;
    private double taxRate;

    public TaxDecorator(BillComponent billComponent, double taxRate) {
        this.wrappedBill = billComponent;
        this.taxRate = taxRate;
    }

    @Override
    public double calculateFinalAmount() {
        double baseAmount = wrappedBill.calculateFinalAmount();
        double taxedAmount = baseAmount * (1 + taxRate / 100);

        // Round to one decimal place
        BigDecimal roundedTaxedAmount = new BigDecimal(taxedAmount).setScale(1, RoundingMode.HALF_UP);

        return roundedTaxedAmount.doubleValue();
    }
}
