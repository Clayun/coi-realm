package com.mcylm.coi.realm.utils;


import com.mcylm.coi.realm.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

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

    /**
     * 扣减玩家背包里的资源数量
     * @param player
     * @param amount
     * @return
     */
    public static boolean deductionResources(Player player, int amount) {
        int playerHadResource = getPlayerHadResource(player);

        // 如果玩家手里的资源数量足够
        if (playerHadResource >= amount) {

            // 扣减物品
            ItemStack[] contents =
                    player.getInventory().getContents();

            // 剩余所需扣减资源数量
            int deductionCount = amount;

            // 资源类型
            Material material = getResourceType();
            for (ItemStack itemStack : contents) {

                if (itemStack == null) {
                    continue;
                }

                // 是资源物品才扣减
                if (itemStack.getType().equals(material)) {
                    // 如果当前物品的堆叠数量大于所需资源，就只扣减数量
                    if (itemStack.getAmount() > deductionCount) {
                        itemStack.setAmount(itemStack.getAmount() - deductionCount);
                        return true;
                    }

                    // 如果当前物品的堆叠数量等于所需资源，就删物品
                    if (itemStack.getAmount() == deductionCount) {
                        player.getInventory().removeItem(itemStack);
                        player.updateInventory();
                        return true;
                    }

                    // 如果物品的堆叠数量小于所需资源，就删物品，同时计数
                    if (itemStack.getAmount() < deductionCount) {
                        // 减去当前物品的库存
                        deductionCount = deductionCount - itemStack.getAmount();
                        player.getInventory().removeItem(itemStack);
                        player.updateInventory();
                    }
                }


            }

        } else
            return false;

        return false;
    }

    /**
     * 获取玩家背包里的资源
     *
     * @return
     */
    public static int getPlayerHadResource(Player player) {

        @NonNull ItemStack[] contents =
                player.getInventory().getContents();

        Material material = getResourceType();
        if (material == null) {
            return 0;
        }

        int num = 0;

        for (ItemStack itemStack : contents) {

            if (itemStack == null) {
                continue;
            }

            if (itemStack.getType().equals(material)) {
                num = num + itemStack.getAmount();
            }
        }

        return num;

    }

    public static Material getResourceType() {
        String materialName = Entry.getInstance().getConfig().getString("game.building.material");

        return Material.getMaterial(materialName);

    }



}


