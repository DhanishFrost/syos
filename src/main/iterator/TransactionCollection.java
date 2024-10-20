package main.iterator;

import java.util.List;

import main.domain.model.Transaction.TransactionItem;

public class TransactionCollection implements IterableAggregate<TransactionItem> {
    private List<TransactionItem> items;

    public TransactionCollection(List<TransactionItem> items) {
        this.items = items;
    }

    @Override
    public Iterator<TransactionItem> createIterator() {
        return new TransactionIterator(items);
    }
}
