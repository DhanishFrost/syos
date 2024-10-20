package main.domain.model;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import main.domain.util.DiscountCalculator;
import main.domain.util.TaxCalculator;
import main.strategies.discount.DiscountStrategy;

public class Bill implements BillComponent {
    private int billId;
    private int customerId;
    private double totalAmount;
    private double cashTendered;
    private double changeGiven;
    private Date billDate;
    private double discountAmount;
    private double taxAmount;
    private double finalPrice;
    private int loyaltyPointsUsed;

    private DiscountCalculator discountCalculator;
    private TaxCalculator taxCalculator;
    private TransactionManager transactionManager;

    private Bill(BillBuilder builder) {
        this.billId = builder.billId;
        this.customerId = builder.customerId;
        this.totalAmount = builder.totalAmount;
        this.cashTendered = builder.cashTendered;
        this.changeGiven = builder.changeGiven;
        this.billDate = builder.billDate;
        this.discountAmount = builder.discountAmount;
        this.taxAmount = builder.taxAmount;
        this.finalPrice = builder.finalPrice;
        this.loyaltyPointsUsed = builder.loyaltyPointsUsed;
        this.discountCalculator = builder.discountCalculator != null ? builder.discountCalculator : new DiscountCalculator(amount -> amount);
        this.taxCalculator = builder.taxCalculator != null ? builder.taxCalculator : new TaxCalculator(5);
        this.transactionManager = builder.transactionManager != null ? builder.transactionManager : new TransactionManager();
    }

    public static class BillBuilder {
        private int billId;
        private int customerId;
        private double totalAmount;
        private double cashTendered;
        private double changeGiven;
        private Date billDate = new Date();
        private double discountAmount;
        private double taxAmount;
        private double finalPrice;
        private int loyaltyPointsUsed;
        private DiscountCalculator discountCalculator;
        private TaxCalculator taxCalculator;
        private TransactionManager transactionManager;

        public BillBuilder withBillId(int billId) {
            this.billId = billId;
            return this;
        }

        public BillBuilder withCustomerId(int customerId) {
            this.customerId = customerId;
            return this;
        }

        public BillBuilder withTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public BillBuilder withCashTendered(double cashTendered) {
            this.cashTendered = cashTendered;
            return this;
        }

        public BillBuilder withBillDate(Date billDate) {
            this.billDate = billDate;
            return this;
        }

        public BillBuilder withDiscountAmount(double discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public BillBuilder withTaxAmount(double taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        public BillBuilder withFinalPrice(double finalPrice) {
            this.finalPrice = finalPrice;
            return this;
        }

        public BillBuilder withChangeGiven(double changeGiven) {
            this.changeGiven = changeGiven;
            return this;
        }

        public BillBuilder withLoyaltyPointsUsed(int loyaltyPointsUsed) {
            this.loyaltyPointsUsed = loyaltyPointsUsed;
            return this;
        }

        public BillBuilder withDiscountCalculator(DiscountCalculator discountCalculator) {
            this.discountCalculator = discountCalculator;
            return this;
        }

        public BillBuilder withTaxCalculator(TaxCalculator taxCalculator) {
            this.taxCalculator = taxCalculator;
            return this;
        }

        public BillBuilder withTransactionManager(TransactionManager transactionManager) {
            this.transactionManager = transactionManager;
            return this;
        }

        public Bill build() {
            return new Bill(this);
        }
    }

    public void clear() {
        this.billId = 0;
        this.customerId = 0;
        this.totalAmount = 0.0;
        this.cashTendered = 0.0;
        this.changeGiven = 0.0;
        this.billDate = new Date();
        this.discountAmount = 0.0;
        this.taxAmount = 0.0;
        this.finalPrice = 0.0;
        this.loyaltyPointsUsed = 0;
        this.transactionManager.clear();
    }

    // Getters
    public int getBillId() {
        return billId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public List<Transaction> getTransactions() {
        return transactionManager.getTransactions();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getCashTendered() {
        return cashTendered;
    }

    public double getChangeGiven() {
        return changeGiven;
    }

    public Date getBillDate() {
        return billDate;
    }

    public String getFormattedBillDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(billDate); 
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public int getLoyaltyPointsUsed() {
        return loyaltyPointsUsed;
    }

    public void setLoyaltyPointsUsed(int loyaltyPointsUsed) {
        this.loyaltyPointsUsed = loyaltyPointsUsed;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discountCalculator.getDiscountStrategy();
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        discountCalculator.setDiscountStrategy(discountStrategy);
    }

    public void addTransaction(Transaction transaction) {
        transactionManager.addTransaction(transaction);
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        totalAmount = transactionManager.getTransactions().stream()
                .mapToDouble(Transaction::getTotalPrice)
                .sum();
    }

    @Override
    public double calculateFinalAmount() {
        return discountCalculator.applyDiscount(totalAmount);
    }

    public double calculateFinalAmountWithTax() {
        return taxCalculator.calculateTax(calculateFinalAmount());
    }

    public void calculateFinalPriceAndChange() {
        double finalAmountWithoutTax = calculateFinalAmount();
        this.discountAmount = roundToOneDecimal(totalAmount - finalAmountWithoutTax);
        this.taxAmount = roundToOneDecimal(calculateFinalAmountWithTax() - finalAmountWithoutTax);
        this.finalPrice = roundToOneDecimal(calculateFinalAmountWithTax() - loyaltyPointsUsed);
        this.changeGiven = roundToOneDecimal(cashTendered - finalPrice);
    }

    public void setCashTendered(double cashTendered) {
        this.cashTendered = cashTendered;
        calculateFinalPriceAndChange();
    }

    private double roundToOneDecimal(double value) {
        return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
