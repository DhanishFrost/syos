package main.factory;

import main.domain.model.Bill;
import main.objectPool.BillPool;

public class BillFactory {
    private final BillPool billPool;

    public BillFactory(BillPool billPool) {
        this.billPool = billPool;
    }

    public Bill createBill() {
        return billPool.borrowObject();
    }

    public void releaseBill(Bill bill) {
        billPool.returnObject(bill);
    }
}
