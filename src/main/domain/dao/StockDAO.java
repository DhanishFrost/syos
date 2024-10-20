package main.domain.dao;

import java.util.List;

import main.domain.model.ItemStock;
import main.observer.Subject;

public interface StockDAO extends Subject {
    List<ItemStock> findStockByItemCode(String itemCode);
    void updateStockQuantity(int itemStockID, int newQuantity);
    int getTotalAvailableQuantity(String itemCode);
    List<ItemStock> findStockBelowReorderLevel(int reorderLevel);
    public List<ItemStock> findAllStockDetails();
    public List<ItemStock> findExpiredStock();
    public void removeExpiredStock(List<ItemStock> expiredStocks);
    void removeStockById(int itemStockID);
}
