package main.scheduler;

import java.util.List;

import main.domain.dao.ShelvesDAO;
import main.domain.dao.StockDAO;
import main.domain.model.ItemStock;
import main.domain.model.Shelf;

public class ExpirationCheckTask implements Runnable {
    private final StockDAO stockDAO;
    private final ShelvesDAO shelvesDAO;
    
    public ExpirationCheckTask(StockDAO stockDAO, ShelvesDAO shelvesDAO) {
        this.stockDAO = stockDAO;
        this.shelvesDAO = shelvesDAO;
    }

    @Override
    public void run() {
        try {
            removeExpiredStock();
            removeExpiredShelves();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeExpiredStock() {
        List<ItemStock> expiredStocks = stockDAO.findExpiredStock();
        stockDAO.removeExpiredStock(expiredStocks);
    }

    private void removeExpiredShelves() {
        List<Shelf> expiredShelves = shelvesDAO.findExpiredShelves();
        shelvesDAO.removeExpiredShelves(expiredShelves);
    }
}
