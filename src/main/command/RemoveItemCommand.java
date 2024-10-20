package main.command;

import main.domain.model.Item;
import main.domain.model.Transaction;

public class RemoveItemCommand implements Command {
    private Transaction transaction;
    private Item item;
    private int itemQuantity;

    public RemoveItemCommand(Transaction transaction, Item item, int itemQuantity) {
        this.transaction = transaction;
        this.item = item;
        this.itemQuantity = itemQuantity;
    }

    @Override
    public void execute() {
        transaction.removeItem(item, itemQuantity);
    }

    @Override
    public void undo() {
        transaction.addItem(item, itemQuantity);
    }
}