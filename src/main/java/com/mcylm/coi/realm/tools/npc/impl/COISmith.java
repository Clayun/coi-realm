package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.runnable.TaskRunnable;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.COIMinerCreator;
import com.mcylm.coi.realm.utils.ChestUtils;
import com.mcylm.coi.realm.utils.InventoryUtils;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.WearUtils;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftMerchant;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 铁匠
 * 自己本身会出售一些实用的东西
 */
public class COISmith extends COIEntity {

    public COISmith(COINpc npcCreator) {
        super(npcCreator);
    }


    @Override
    public void move(){
        super.move();

        // 看最近的玩家
        lookNearestPlayer();

        if(!isTooHungryToWork()){
            Location center = getCoiNpc().getSpawnLocation().clone();
            center.setX(center.getX() + 0.5);
            center.setZ(center.getZ() + 0.5);
            findPath(center);
        }

    }

    @Override
    public int delayTick() {
        return 10;
    }
}
