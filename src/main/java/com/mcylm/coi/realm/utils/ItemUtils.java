package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.Nullable;

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
        m.setDisplayName(LoggerUtils.replaceColor(s));
        i.setItemMeta(m);
    }

    public static String getName(ItemStack i) {
        ItemMeta m = i.getItemMeta();
        return m.getDisplayName();
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
    public static Map<Integer, ItemStack> addItemIntoContainer(Location location, ItemStack itemStack){

        Block block = location.getBlock();

        if (block.getState() instanceof Container container){

            container.getSnapshotInventory().addItem(itemStack);
            HashMap<Integer, ItemStack> extraItems = container.getInventory().addItem(itemStack);

            container.update();

            return extraItems;
        }else{
            LoggerUtils.debug("这是个"+block.getType().name());
            return new HashMap<>();
        }
    }

    /**
     * 获取箱子里某样物品的总数量
     * @param location
     * @param material
     * @return
     */
    public static int getItemAmountFromContainer(Location location,Material material){

        int count = 0;

        Block block = location.getBlock();

        if (block.getState() instanceof Container container){

            @Nullable ItemStack[] contents = container.getInventory().getContents();
            if(contents.length > 0){
                for(ItemStack item : contents){
                    if(item != null && item.getType().equals(material)){
                        count = count + item.getAmount();
                    }
                }
            }

        }

        return count;
    }

    /**
     * 获取背包里面某样物品的数量
     * @param inventory
     * @param material
     * @return
     */
    public static int getItemAmountFromInventory(Inventory inventory,Material material){

        int count = 0;

        @Nullable ItemStack[] contents = inventory.getContents();
        if(contents.length > 0){
            for(ItemStack item : contents){
                if(item != null && item.getType().equals(material)){
                    count = count + item.getAmount();
                }
            }
        }

        return count;
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
                for (ItemStack itemStack : chest.getSnapshotInventory()) {

                    if (surplusNum == 0) {
                        break;
                    }

                    ItemStack next = itemStack;

                    if (next != null) {
                        if (next.getType().equals(material)) {
                            // 如果类型一致，就尝试扣减
                            int amount = next.getAmount();

                            if (amount > surplusNum) {
                                // 如果物品数量大于剩余待扣减数量，就直接扣减数量
                                amount = amount - surplusNum;
                                next.setAmount(amount);

                                // 计数
                                count = count + surplusNum;
                            }

                            if (amount == surplusNum) {
                                // 如果物品数量等于剩余待扣减数量，就直接删除物品
                                surplusNum = 0;
                                chest.getSnapshotInventory().remove(next);

                                // 计数
                                count = count + surplusNum;
                            }

                            if (amount < surplusNum) {
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

    /**
     * 扣减资源
     * @param amount
     * @param inventory
     * @return
     */
    public static int deductionResources(int amount,Inventory inventory) {

        String materialName = Entry.getInstance().getConfig().getString("game.building.material");
        int playerHadResource = ItemUtils.getItemAmountFromInventory(inventory,Material.getMaterial(materialName));

        // 如果玩家手里的资源数量足够
        if (playerHadResource >= amount) {

            // 扣减物品
            ItemStack[] contents =
                    inventory.getContents();

            // 剩余所需扣减资源数量
            int deductionCount = amount;

            // 资源类型
            Material material = Material.getMaterial(materialName);
            for (ItemStack itemStack : contents) {

                if (itemStack == null) {
                    continue;
                }

                // 是资源物品才扣减
                if (itemStack.getType().equals(material)) {
                    // 如果当前物品的堆叠数量大于所需资源，就只扣减数量
                    if (itemStack.getAmount() > deductionCount) {
                        itemStack.setAmount(itemStack.getAmount() - deductionCount);
                        return deductionCount;
                    }

                    // 如果当前物品的堆叠数量等于所需资源，就删物品
                    if (itemStack.getAmount() == deductionCount) {
                        inventory.removeItem(itemStack);
                        return deductionCount;
                    }

                    // 如果物品的堆叠数量小于所需资源，就删物品，同时计数
                    if (itemStack.getAmount() < deductionCount) {
                        // 减去当前物品的库存
                        deductionCount = deductionCount - itemStack.getAmount();
                        inventory.removeItem(itemStack);
                    }
                }


            }

        } else
            return 0;

        return 0;
    }
}