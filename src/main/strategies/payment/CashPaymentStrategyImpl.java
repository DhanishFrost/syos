package main.strategies.payment;

import main.domain.model.Bill;

public class CashPaymentStrategyImpl implements PaymentStrategy {
    private double cashTendered;

    public CashPaymentStrategyImpl(double cashTendered) {
        this.cashTendered = cashTendered;
    }

    @Override
    public void pay(double amount, Bill bill) {
        
        // Update the Bill object with cash tendered and change
        bill.setCashTendered(cashTendered);  
        bill.calculateFinalPriceAndChange();
    }
}
