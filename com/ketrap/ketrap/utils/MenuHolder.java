package com.ketrap.ketrap.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder
implements InventoryHolder {
    private final String menuType;

    public MenuHolder(String menuType) {
        this.menuType = menuType;
    }

    public String getMenuType() { return this.menuType; }

    public Inventory getInventory() { return null; }
}
