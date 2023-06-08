package com.mcylm.coi.realm.tools.npc.impl.monster;

import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;
import com.mcylm.coi.realm.utils.LoggerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class COIPillager extends COIMonster {


    public COIPillager(COIPillagerCreator npcCreator) {
        super(npcCreator);
    }

    @Override
    public int getDamage() {
        return 5;
    }

    @Override
    public int delayTick() {
        return 5;
    }

    @Override
    public void spawn(Location location) {

        super.spawn(location);

        setHunger(500);
        // 初始化背包
        getCoiNpc().getInventory().addItem(new ItemStack(Material.CROSSBOW));
    }
}
