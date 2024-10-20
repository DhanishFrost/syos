package main.factory;

import main.domain.model.Transaction;
import main.objectPool.TransactionPool;

public class TransactionFactory {
    private final TransactionPool transactionPool;

    public TransactionFactory(TransactionPool transactionPool) {
        this.transactionPool = transactionPool;
    }

    public Transaction createTransaction() {
        return transactionPool.borrowObject();
    }

    public void releaseTransaction(Transaction transaction) {
        transactionPool.returnObject(transaction);
    }
}
