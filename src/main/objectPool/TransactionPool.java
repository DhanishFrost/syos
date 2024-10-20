package main.objectPool;

import java.util.Stack;

import main.domain.model.Transaction;

public class TransactionPool implements ObjectPool<Transaction> {
    private static final int MAX_POOL_SIZE = 200;
    private Stack<Transaction> pool;

    public TransactionPool() {
        pool = new Stack<>();
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            pool.push(new Transaction());
        }
    }

    @Override
    public Transaction borrowObject() {
        if (pool.isEmpty()) {
            return new Transaction();
        }
        Transaction transaction = pool.pop();
        transaction.clear(); // Clear the state before reusing
        return transaction;
    }

    @Override
    public void returnObject(Transaction transaction) {
        if (pool.size() < MAX_POOL_SIZE) {
            transaction.clear(); // Clear the state before returning to the pool
            pool.push(transaction);
        }
    }
}
