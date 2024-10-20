package main.scheduler;

public interface ReplenishShelves {
    void replenishShelves(String itemCode);
    void endOfDayReplenishment();
}
