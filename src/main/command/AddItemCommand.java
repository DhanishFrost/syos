package main.command;

import main.domain.model.Item;
import main.domain.model.Transaction;

public class AddItemCommand implements Command {
    private Transaction transaction;
    private Item item;
    private int itemQuantity;

    public AddItemCommand(Transaction transaction, Item item, int itemQuantity) {
        this.transaction = transaction;
        this.item = item;
        this.itemQuantity = itemQuantity;
    }

    @Override
    public void execute() {
        transaction.addItem(item, itemQuantity);
    }

    @Override
    public void undo() {
        transaction.removeItem(item, itemQuantity);
    }
}
