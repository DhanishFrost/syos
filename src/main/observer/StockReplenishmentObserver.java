package main.observer;

import main.scheduler.ReplenishShelves;

public class StockReplenishmentObserver implements Observer {
    private ReplenishShelves replenishShelves;
    private int threshold;

    public StockReplenishmentObserver(ReplenishShelves replenishShelves, int threshold) {
        this.replenishShelves = replenishShelves;
        this.threshold = threshold;
    }

    @Override
    public void update(String itemCode, int newQuantity) {
        if (newQuantity < threshold) {
            replenishShelves.replenishShelves(itemCode);
        }
    }
}
