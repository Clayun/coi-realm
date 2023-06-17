package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TeamFollowGoal extends SimpleGoal {

    private AttackTeam team;
    private LivingEntity followingEntity;

    // 保持饥饿度到多少
    private int keepHunger = 20;

    // 每次投喂多少个
    private int feedNum = 2;

    // 2秒扣一次，避免给旋了
    private int skipFeedAction = 0;
    public TeamFollowGoal(Commandable npc, AttackTeam team) {
        super(npc);
        this.team = team;
    }

    @Override
    public void tick() {
        Commandable npc = getExecutor();

        int index = team.getMembers().indexOf((COIEntity) npc);

        if (npc.getTarget() == null && followingEntity != null) {
            if (followingEntity.isDead() && index > 0) {
                team.getMembers().remove(index - 1);
            }
            if (npc.getLocation() != null && npc.getLocation().distance(npc.getCommander().getLocation()) >= 3) {
                npc.findPath(followingEntity.getLocation());
            }
        }


        COIEntity entity = (COIEntity) npc;
        if (entity.isAlive() && entity.getHunger() < keepHunger) {
            if (npc.getCommander() instanceof Player player) {

                if (skipFeedAction == 40) {
                    skipFeedAction = 0;
                }

                if (skipFeedAction == 0) {
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

        getExecutor().lookForEnemy(-1);
    }

    @Override
    public void asyncTick() {

        COIEntity entity = (COIEntity) getExecutor();

        int index = team.getMembers().indexOf(entity);
        if (index == 0) {
            followingEntity = team.getCommander();
        } else {
            followingEntity = (LivingEntity) team.getMembers().get(index - 1).getNpc().getEntity();
        }



    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.TEAM_FOLLOW;
    }


}
