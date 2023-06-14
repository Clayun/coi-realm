package com.mcylm.coi.realm.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WearUtils {

    public static boolean canWearOnHead(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        Material type = itemStack.getType();
        return type == Material.LEATHER_HELMET || type == Material.CHAINMAIL_HELMET ||
                type == Material.IRON_HELMET || type == Material.GOLDEN_HELMET ||
                type == Material.DIAMOND_HELMET || type == Material.TURTLE_HELMET ||
                type == Material.NETHERITE_HELMET;
    }

    // 判断物品是否可以穿在身上
    public static boolean canWearOnBody(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        Material material = itemStack.getType();
        return material == Material.LEATHER_CHESTPLATE
                || material == Material.CHAINMAIL_CHESTPLATE
                || material == Material.IRON_CHESTPLATE
                || material == Material.GOLDEN_CHESTPLATE
                || material == Material.DIAMOND_CHESTPLATE
                || material == Material.NETHERITE_CHESTPLATE;
    }

    // 判断物品是否可以穿在腿上
    public static boolean canWearOnLegs(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        Material material = itemStack.getType();
        return material == Material.LEATHER_LEGGINGS
                || material == Material.CHAINMAIL_LEGGINGS
                || material == Material.IRON_LEGGINGS
                || material == Material.GOLDEN_LEGGINGS
                || material == Material.DIAMOND_LEGGINGS
                || material == Material.NETHERITE_LEGGINGS
                ;
    }

    // 判断物品是否可以穿在脚上
    public static boolean canWearOnFeet(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        Material material = itemStack.getType();
        return material == Material.LEATHER_BOOTS
                || material == Material.CHAINMAIL_BOOTS
                || material == Material.IRON_BOOTS
                || material == Material.GOLDEN_BOOTS
                || material == Material.DIAMOND_BOOTS
                || material == Material.NETHERITE_BOOTS
                ;
    }

}
