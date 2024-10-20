package main.domain.dao;

import main.domain.model.Item;

public interface ItemDAO {
    Item findItemByCode(String code);
}