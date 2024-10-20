package main.strategies.payment;

import main.domain.model.Bill; 

public interface PaymentStrategy {
    void pay(double amount, Bill bill); 
}
