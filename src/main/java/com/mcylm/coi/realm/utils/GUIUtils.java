package com.mcylm.coi.realm.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIUtils {

    JavaPlugin plugin;
    Inventory inv;

    public GUIUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    public static Inventory createNpcInventory(int line) {
        Inventory inventory = Bukkit.createInventory(null, line * 9, "NPC");
        return inventory;
    }
    public Inventory createGUI(HashMap<Integer, ItemStack> slots,
                               String invName) {

        Inventory inv = plugin.getServer().createInventory(null, 45, invName);

        for (Integer i : slots.keySet()) {

            inv.setItem(i, slots.get(i));

        }
        this.inv = inv;
        return inv;

    }

    public void openGUI(Player p) {
        p.openInventory(inv);
    }

    public static Inventory createGUI(JavaPlugin plugin,
                                      HashMap<Integer, ItemStack> slots, String invName) {

        Inventory inv = plugin.getServer().createInventory(null, 45, invName);

        for (Integer i : slots.keySet()) {

            inv.setItem(i, slots.get(i));

        }

        return inv;

    }

    public static Inventory createGUI(JavaPlugin plugin,
                                      HashMap<Integer, ItemStack> slots, String invName, int slotsNum) {

        Inventory inv = plugin.getServer().createInventory(null, slotsNum, invName);

        for (Integer i : slots.keySet()) {

            inv.setItem(i, slots.get(i));

        }

        return inv;

    }

    public static void openGUI(Player p, Inventory inv) {
        p.openInventory(inv);
    }

    public static void closeGUI(Player p) {
        p.closeInventory();
    }

    public static List<String> getStrList(String inputString) {

        if(StringUtils.isBlank(inputString)){
            return new ArrayList<>();
        }

        int length = 15;
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    private static List<String> getStrList(String inputString, int length,
                                           int size) {
        List<String> list = new ArrayList<String>();


        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    private static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    public static String getStarByLevel(int level){
        StringBuilder stringBuilder = new StringBuilder("");
        for(int i = 0; i < level; i++){
            stringBuilder.append("▌");
        }

        return stringBuilder.toString();
    }

}