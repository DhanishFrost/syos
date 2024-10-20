package main.scheduler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

import main.domain.dao.ShelvesDAO;
import main.domain.dao.StockDAO;
import main.domain.model.ItemStock;
import main.domain.model.Shelf;

import java.sql.Date;

public class ReplenishShelvesImpl implements ReplenishShelves {
    private final StockDAO stockDAO;
    private final ShelvesDAO shelvesDAO;
    private final ReshelvingEventLogger reshelvingEventLogger;
    private final Map<String, Integer> shelfQuantityCache = new HashMap<>();
    private final int minShelfQuantity;
    private final int maxShelfCapacity;
    private final int endOfDayShelfQuantity;
    private final Lock lock = new ReentrantLock(); // Lock for thread safety
    private static final long TIMEOUT_DURATION = 2; // 2 seconds

    public ReplenishShelvesImpl(StockDAO stockDAO, ShelvesDAO shelvesDAO,
            ReshelvingEventLogger reshelvingEventLogger, int minQty, int maxCapacity, int endOfDayQty) {
        this.stockDAO = stockDAO;
        this.shelvesDAO = shelvesDAO;
        this.reshelvingEventLogger = reshelvingEventLogger;
        this.minShelfQuantity = minQty;
        this.maxShelfCapacity = maxCapacity;
        this.endOfDayShelfQuantity = endOfDayQty;
    }

    @Override
    public void replenishShelves(String itemCode) {
        try {
            if (lock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    // Always fetch the latest shelf quantity from the database
                    List<Shelf> shelves = shelvesDAO.findShelvesByItemCode(itemCode);
                    int currentQuantity = shelves.stream().mapToInt(Shelf::getQuantity).sum();

                    if (currentQuantity < minShelfQuantity) {
                        performReplenishment(itemCode, currentQuantity, maxShelfCapacity);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                System.err.println("Could not acquire lock to replenish shelves for item: " + itemCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while trying to lock for item: " + itemCode);
        } catch (Exception e) {
            System.err.println("Error during shelf replenishment for item code: " + itemCode);
            e.printStackTrace();
        }
    }

    private void performReplenishment(String itemCode, int currentQuantity, int targetQuantity) {
        try {
            if (lock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    // Re-fetch the latest shelf quantity after acquiring the lock
                    List<Shelf> updatedShelves = shelvesDAO.findShelvesByItemCode(itemCode);
                    int updatedCurrentQuantity = updatedShelves.stream().mapToInt(Shelf::getQuantity).sum();

                    // Recheck if replenishment is still needed based on the updated quantity
                    if (updatedCurrentQuantity >= targetQuantity) {
                        System.out.println("Replenishment not needed, updated current quantity: "
                                + updatedCurrentQuantity + " for item: " + itemCode);
                        return; // Exit, no need for replenishment
                    }

                    // Fetch stock items to replenish
                    List<ItemStock> stocks = stockDAO.findStockByItemCode(itemCode);
                    if (stocks == null || stocks.isEmpty()) {
                        System.out.println("No stocks found for item code: " + itemCode);
                        return;
                    }

                    stocks.sort(Comparator.comparing(ItemStock::getStockExpiryDate)
                            .thenComparing(ItemStock::getStockDateOfPurchase));

                    // Recalculate needed quantity based on the rechecked shelf quantity
                    int neededQuantity = Math.min(maxShelfCapacity - updatedCurrentQuantity,
                            targetQuantity - updatedCurrentQuantity);

                    for (ItemStock stock : stocks) {
                        if (neededQuantity <= 0)
                            break;

                        int quantityToMove = Math.min(neededQuantity, stock.getQuantity());
                        Date currentDate = new Date(System.currentTimeMillis());

                        // Check for an existing shelf with the same expiry date
                        Shelf existingShelf = shelvesDAO.findShelfByItemCodeAndExpiryDate(itemCode,
                                stock.getStockExpiryDate());
                        if (existingShelf != null) {
                            existingShelf.setQuantity(existingShelf.getQuantity() + quantityToMove);
                            existingShelf.setMovedDate(currentDate);
                            shelvesDAO.updateShelf(existingShelf);
                        } else {
                            Shelf newShelf = new Shelf(itemCode, quantityToMove, currentDate,
                                    stock.getStockExpiryDate());
                            shelvesDAO.addShelf(newShelf);
                        }

                        stockDAO.updateStockQuantity(stock.getItemStockID(), stock.getQuantity() - quantityToMove);
                        neededQuantity -= quantityToMove;

                        // Update the cache and log the event
                        shelfQuantityCache.put(itemCode, shelfQuantityCache.getOrDefault(itemCode, 0) + quantityToMove);
                        reshelvingEventLogger.logReshelvingEvent(itemCode, quantityToMove);
                    }
                } finally {
                    lock.unlock(); // Release lock after replenishment
                }
            } else {
                System.err.println("Could not acquire lock for replenishing shelves for item: " + itemCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while replenishing shelves for item: " + itemCode);
        }
    }

    public void endOfDayReplenishment() {
        try {
            if (lock.tryLock(TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                try {
                    List<String> itemCodes = shelvesDAO.getAllItemCodes();
                    if (itemCodes == null || itemCodes.isEmpty()) {
                        System.out.println("No item codes found for end-of-day replenishment.");
                        return;
                    }

                    System.out.println("End of day replenishment for " + itemCodes.size() + " items");
                    for (String itemCode : itemCodes) {
                        List<Shelf> shelves = shelvesDAO.findShelvesByItemCode(itemCode);
                        int currentQuantity = (shelves == null || shelves.isEmpty()) ? 0
                                : shelves.stream().mapToInt(Shelf::getQuantity).sum();
                        shelfQuantityCache.put(itemCode, currentQuantity);
                        performReplenishment(itemCode, currentQuantity, endOfDayShelfQuantity);
                        reshelvingEventLogger.logReshelvingEvent(itemCode, endOfDayShelfQuantity - currentQuantity);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                System.err.println("Could not acquire lock for end-of-day replenishment.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted during end-of-day replenishment.");
        } catch (Exception e) {
            System.err.println("Error during end-of-day replenishment.");
            e.printStackTrace();
        }
    }
}
