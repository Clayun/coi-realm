package com.mcylm.coi.realm.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

//    Inspired by Bentipa

    private ItemStack is;

    public ItemUtils(ItemStack is) {
        this.is = is;
    }

    public ItemUtils rename(String s) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(s);
        is.setItemMeta(im);
        return this;
    }

    public ItemUtils setLore(List<String> lore){
        ItemMeta m = is.getItemMeta();
        m.setLore(lore);
        is.setItemMeta(m);
        return this;
    }

    public ItemUtils addLore(String lore) {
        ItemMeta m = is.getItemMeta();
        ArrayList<String> lores = new ArrayList<String>(m.getLore() == null ? new ArrayList<String>() : m.getLore());
        lores.add(lore);
        m.setLore(lores);
        is.setItemMeta(m);
        return this;
    }

    public ItemUtils addEnchantment(Enchantment e, int level) {
        is.addUnsafeEnchantment(e, level);
        return this;
    }

    public ItemStack getItemStack() {
        return is;
    }

    public static void rename(ItemStack i, String s) {
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(s);
        i.setItemMeta(m);
    }

    public static void setLore(ItemStack i, List<String> lore) {
        ItemMeta m = i.getItemMeta();
        m.setLore(lore);
        i.setItemMeta(m);
    }

    public static void addLore(ItemStack is, String lore) {
        ItemMeta m = is.getItemMeta();
        ArrayList<String> lores = new ArrayList<String>(m.getLore() == null ? new ArrayList<String>() : m.getLore());
        lores.add(LoggerUtils.replaceColor(lore));
        m.setLore(lores);
        is.setItemMeta(m);
    }

    public static void addContent(ItemStack is, String lore) {
        ItemMeta m = is.getItemMeta();
        ArrayList<String> lores = new ArrayList<String>(m.getLore() == null ? new ArrayList<String>() : m.getLore());

        List<String> strList = GUIUtils.getStrList(lore);

        for(String string : strList){
            lores.add(LoggerUtils.replaceColor("&b"+string));
        }

        m.setLore(lores);
        is.setItemMeta(m);
    }

    public static void addEnchantment(ItemStack i, Enchantment e, int level) {
        i.addUnsafeEnchantment(e, level);
    }

    /**
     * 将物品添加到箱子里面
     * @param location
     * @param itemStack
     */
    public static void addItemIntoChest(Location location, ItemStack itemStack){

        Block block = location.getBlock();

        if(block.getType().equals(Material.CHEST)){
            Chest chest = (Chest) block.getState();

            chest.update();

            chest.getInventory().addItem(itemStack);
        }
    }
}