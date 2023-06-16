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

    // 保持饥饿度到多少
    private int keepHunger = 20;

    // 每次投喂多少个
    private int feedNum = 2;

    // 2秒扣一次，避免给旋了
    private int skipFeedAction = 0;

    @Override
    public void tick() {
        Commandable npc = getExecutor();

        if (npc.getTarget() == null && npc.getCommander() != null) {
            if (npc.getLocation() != null && npc.getLocation().distance(npc.getCommander().getLocation()) >= maxRadius) {
                npc.findPath(npc.getCommander().getLocation());
            }
        }

        if (npc instanceof COIEntity entity) {
            if (entity.isAlive() && entity.getHunger() < keepHunger) {
                if (npc.getCommander() instanceof Player player) {

                    if(skipFeedAction == 40){
                        skipFeedAction = 0;
                    }

                    if(skipFeedAction == 0){
                        @NotNull HashMap<Integer, ItemStack> extra = player.getInventory().removeItem(new ItemStack(Material.BREAD, feedNum));
                        if (extra.isEmpty()) {
                            entity.addItemToInventory(new ItemStack(Material.BREAD, feedNum));
                        } else {
                            extra.values().forEach(item -> {
                                if (feedNum - item.getAmount() > 0) {
                                    entity.addItemToInventory(new ItemStack(Material.BREAD, feedNum - item.getAmount()));
                                }
                            });
                        }
                    }

                    skipFeedAction++;

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
