package com.mcylm.coi.realm.utils;


import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {
    private static final Inventory testInventory = Bukkit.createInventory(null, 54);

    public static boolean canInventoryHoldItem(Inventory inventory, ItemStack... itemStacks) {
        testInventory.clear();
        ItemStack[] contents = inventory.getContents();
        testInventory.setContents(contents);
        return testInventory.addItem(itemStacks).isEmpty();
    }
}


