package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class FollowGoal extends SimpleGoal {

    public FollowGoal(Commandable npc) {
        super(npc);
    }

    // 跟随范围，超出范围会跟上玩家
    private int maxRadius = 5;

    @Override
    public void tick() {
        Commandable npc = getExecutor();

        if (npc.getTarget() == null && npc.getCommander() != null) {
            if (npc.getLocation() != null && npc.getLocation().distance(npc.getCommander().getLocation()) >= maxRadius) {
                npc.findPath(npc.getCommander().getLocation());
            }
        }

        if (npc instanceof COIEntity entity) {
            if (entity.isAlive() && entity.isTooHungryToWork()) {
                if (npc.getCommander() instanceof Player player) {
                    @NotNull HashMap<Integer, ItemStack> extra = player.getInventory().removeItem(new ItemStack(Material.BREAD, 10));
                    if (extra.isEmpty()) {
                        entity.addItemToInventory(new ItemStack(Material.BREAD, 10));
                    } else {
                        extra.values().forEach(item -> {
                            if (10 - item.getAmount() > 0) {
                                entity.addItemToInventory(new ItemStack(Material.BREAD, 10 - item.getAmount()));
                            }
                        });
                    }
                }
            }
        }

        getExecutor().lookForEnemy(maxRadius);
    }

    @Override
    public void asyncTick() {

    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.FOLLOW;
    }
}
