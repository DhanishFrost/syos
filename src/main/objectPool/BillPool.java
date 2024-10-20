package main.objectPool;

import java.util.Stack;

import main.domain.model.Bill;

public class BillPool implements ObjectPool<Bill> {
    private static final int MAX_POOL_SIZE = 100;
    private Stack<Bill> pool;

    public BillPool() {
        pool = new Stack<>();
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            pool.push(new Bill.BillBuilder().build());
        }
    }

    @Override
    public Bill borrowObject() {
        if (pool.isEmpty()) {
            return new Bill.BillBuilder().build();
        }
        Bill bill = pool.pop();
        bill.clear(); // Clear the state before reusing
        return bill;
    }

    @Override
    public void returnObject(Bill bill) {
        if (pool.size() < MAX_POOL_SIZE) {
            bill.clear(); // Clear the state before returning to the pool
            pool.push(bill);
        }
    }
}
