package main.iterator;

import java.util.List;

import main.domain.model.Transaction;

public class BillIterator implements Iterator<Transaction> {
    private List<Transaction> transactions;
    private int position;

    public BillIterator(List<Transaction> transactions) {
        this.transactions = transactions;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < transactions.size();
    }

    @Override
    public Transaction next() {
        return transactions.get(position++);
    }
}
