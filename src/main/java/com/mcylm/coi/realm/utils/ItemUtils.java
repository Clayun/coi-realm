package com.mcylm.coi.realm.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class ItemUtils {

//    Inspired by Bentipa

    public static Set<Material> SUITABLE_CONTAINER_TYPES = EnumSet.of(Material.CHEST, Material.BARREL, Material.SHULKER_BOX);

    private ItemStack is;

    public ItemUtils(ItemStack is) {
        this.is = is;
    }

    public ItemUtils rename(String s,TextColor textColor) {
        ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(s, textColor));
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
    public static void addItemIntoContainer(Location location, ItemStack itemStack){

        Block block = location.getBlock();

        if(block.getState() instanceof Container container){

            container.getSnapshotInventory().addItem(itemStack);
            container.getInventory().addItem(itemStack);

            container.update();

        }else{
            LoggerUtils.debug("这是个"+block.getType().name());
        }
    }


    /**
     * 从箱子里拿指定数量的物品
     * @param location 箱子位置
     * @param material 拿取的物品材质
     * @param num 实际拿取的数量
     * @return
     */
    public static int takeItemFromChest(Location location, Material material,int num){

        Block block = location.getBlock();

        // 剩余待扣减数量
        int surplusNum = num;

        // 已扣减数量
        int count = 0;

        if(block.getType().equals(Material.CHEST)){
            Chest chest = (Chest) block.getState();

            // 如果箱子里包含物品材质，就尝试扣减
            if(chest.getSnapshotInventory().contains(material)){
                ListIterator<ItemStack> iterator = chest.getSnapshotInventory().iterator();
                while(iterator.hasNext()){

                    if(surplusNum == 0){
                        break;
                    }

                    ItemStack next = iterator.next();

                    if(next != null){
                        if(next.getType().equals(material)){
                            // 如果类型一致，就尝试扣减
                            int amount = next.getAmount();

                            if(amount > surplusNum){
                                // 如果物品数量大于剩余待扣减数量，就直接扣减数量
                                amount = amount - surplusNum;
                                next.setAmount(amount);

                                // 计数
                                count = count + surplusNum;
                            }

                            if(amount == surplusNum){
                                // 如果物品数量等于剩余待扣减数量，就直接删除物品
                                surplusNum = 0;
                                chest.getSnapshotInventory().remove(next);

                                // 计数
                                count = count + surplusNum;
                            }

                            if(amount < surplusNum){
                                // 如果物品数量小于待扣减数量，同时删除物品
                                surplusNum = surplusNum - amount;
                                chest.getSnapshotInventory().remove(next);

                                // 计数
                                count = count + amount;
                            }
                        }
                    }



                }
            }

            chest.update();

            return count;

        }else{
            LoggerUtils.debug("这是个"+block.getType().name());
            return 0;
        }

    }

    public static void changeColorForLeather(ItemStack item,Color color){
        LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
    }
}