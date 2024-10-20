package main.domain.dao;

import java.util.List;

import main.domain.model.Shelf;

public interface ShelvesDAO  {
    List<Shelf> findShelvesByItemCode(String itemCode);
    void addOrUpdateShelf(Shelf shelf);
    List<String> getAllItemCodes();
    Shelf findShelfByItemCodeAndExpiryDate(String itemCode, java.sql.Date expiryDate);
    public void updateShelf(Shelf shelf);
    public void addShelf(Shelf shelf);
    public void updateShelfQuantityAndRemoveIfZero(String itemCode, int quantitySold);
    public void deleteShelf(Shelf shelf);
    public List<Shelf> findExpiredShelves();
    public void removeExpiredShelves(List<Shelf> expiredShelves);
}
