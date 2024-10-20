package main.iterator;

import java.util.List;

import main.domain.model.Transaction.TransactionItem;

public class TransactionIterator implements Iterator<TransactionItem> {
    private List<TransactionItem> items;
    private int position;

    public TransactionIterator(List<TransactionItem> items) {
        this.items = items;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < items.size();
    }

    @Override
    public TransactionItem next() {
        if (!hasNext()) {
            throw new IllegalStateException("No more items to iterate over.");
        }
        return items.get(position++);
    }
}
