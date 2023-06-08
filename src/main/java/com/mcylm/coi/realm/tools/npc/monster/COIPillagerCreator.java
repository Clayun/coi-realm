package com.mcylm.coi.realm.tools.npc.monster;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.impl.PatrolGoal;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.impl.monster.COIPillager;
import com.mcylm.coi.realm.utils.GUIUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class COIPillagerCreator extends COIMonsterCreator{
    @Setter
    @Getter
    private AttackTeam attackteam;
    public COIPillagerCreator(AttackTeam attackteam) {
        setNpcType(EntityType.PILLAGER);
        this.attackteam = attackteam;
    }

    public static COIPillagerCreator initCOIPillagerCreator(AttackTeam attackteam) {
        // 背包内的物品
        Inventory inventory = GUIUtils.createNpcInventory(3);
        inventory.addItem(new ItemStack(Material.CROSSBOW));

        ItemStack itemStack = new ItemStack(Material.BREAD);
        itemStack.setAmount(200);
        inventory.addItem(itemStack);

        // 不破坏方块
        Set<String> breakBlockMaterials = new HashSet<>();

        // 捡起的东西
        Set<String> pickItemMaterials = new HashSet<>();
        pickItemMaterials.add("EMERALD");

        // 将装备默认设为捡起
        List<Material> clothes = COINpc.CLOTHES;
        for(Material clothesType : clothes){
            pickItemMaterials.add(clothesType.name());
        }
        COIPillagerCreator npcCreator = new COIPillagerCreator(attackteam);
        npcCreator.setInventory(inventory);
        npcCreator.setAggressive(true);
        npcCreator.setAlertRadius(10);
        npcCreator.setBreakBlockMaterials(breakBlockMaterials);
        npcCreator.setName("掠夺者");
        npcCreator.setLevel(1);
        npcCreator.setPickItemMaterials(pickItemMaterials);
        return npcCreator;
    }

    @Override
    public void createMonster(COIBuilding building) {
        COIPillager pillager = new COIPillager(this);
        pillager.spawn(getSpawnLocation());

        pillager.setGoal(new PatrolGoal(pillager)); //TODO 使用TeamFollowGoal
        pillager.getGoal().start();
    }
}
