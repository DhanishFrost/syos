package main.scheduler;

import main.domain.dao.StockDAO;
import main.domain.model.ItemStock;

import java.util.List;

public class DailyStockCheckTask implements Runnable {
    private StockDAO stockDAO;
    private int threshold;

    public DailyStockCheckTask(StockDAO stockDAO, int threshold) {
        this.stockDAO = stockDAO;
        this.threshold = threshold;
    }

    @Override
    public void run() {
        List<ItemStock> itemsBelowThreshold = stockDAO.findStockBelowReorderLevel(threshold);
        if (itemsBelowThreshold != null) {
            for (ItemStock itemStock : itemsBelowThreshold) {
                System.out.println();
                System.out.println("Item " + itemStock.getCode() + " is below reorder level: Quantity: "
                        + itemStock.getQuantity());
            }
        }
    }
}
