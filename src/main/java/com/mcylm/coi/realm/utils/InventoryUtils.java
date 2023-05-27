package com.mcylm.coi.realm.utils;


import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryUtils {
    private static final Map<InventoryType, Inventory> testInventories = new HashMap<>();
    public static boolean canInventoryHoldItem(Inventory inventory, ItemStack... itemStacks) {
        Inventory testInventory = testInventories.get(inventory.getType());
        if (testInventory == null) {
            testInventory = Bukkit.createInventory(null, inventory.getType());
            testInventories.put(inventory.getType(), testInventory);
        }
        testInventory.clear();
        ItemStack[] contents = inventory.getContents();
        testInventory.setContents(contents);
        return testInventory.addItem(itemStacks).isEmpty();
    }
}


