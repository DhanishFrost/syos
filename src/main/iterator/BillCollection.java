package main.iterator;

import java.util.List;

import main.domain.model.Transaction;

public class BillCollection implements IterableAggregate<Transaction> {
    private List<Transaction> transactions;

    public BillCollection(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public Iterator<Transaction> createIterator() {
        return new BillIterator(transactions);
    }
}
