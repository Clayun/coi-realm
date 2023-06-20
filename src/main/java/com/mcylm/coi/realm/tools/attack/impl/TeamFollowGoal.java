package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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

    // 最大的半径范围
    private int maxRadius = 30;

    public TeamFollowGoal(Commandable npc, AttackTeam team) {
        super(npc);
        this.team = team;
    }

    @Override
    public void tick() {

        Commandable npc = getExecutor();


        if (npc.getLocation() == null && followingEntity != null) {
            quitTeam();
            return;
        }

        if (npc.getLocation().distance(team.getCommander().getLocation()) > maxRadius * 1.4) {
            quitTeam();
            return;
        }

        // int index = team.getMembers().indexOf((COIEntity) npc);

        boolean needFollow;
        if (npc.getTarget() == null && followingEntity != null && npc.getLocation().distance(followingEntity.getLocation()) > 2) {
            needFollow = true;
        } else {
            needFollow = false;
        }
        if (followingEntity != null && npc.getLocation().distance(followingEntity.getLocation()) > maxRadius) {
            needFollow = true;
        }
        if (needFollow) {
            npc.setTarget(null);
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

        getExecutor().lookForEnemy(maxRadius);

    }

    @Override
    public void asyncTick() {

        COIEntity entity = (COIEntity) getExecutor();


        if (team.getCommander() instanceof Player player) {
            if (!player.isOnline()) {
                quitTeam();
            }
        }

        int index = team.getMembers().indexOf(entity);
        if (index == -1) {
            quitTeam();
            return;
        }
        if (index == 0) {
            followingEntity = team.getCommander();
        } else {
            for (int i = index; i >= 0 ; i--) {
                COIEntity member = team.getMembers().get(i);
                if (member.isAlive() && member != this.getExecutor()) {
                    followingEntity = (LivingEntity) team.getMembers().get(i).getNpc().getEntity();
                    break;
                }
            }
        }



    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.TEAM_FOLLOW;
    }

    public void quitTeam() {

        if (getExecutor() instanceof COIEntity entity) {
            getExecutor().setGoal(new PatrolGoal(getExecutor()));
            getExecutor().getGoal().start();
            team.getMembers().remove(entity);
        }
    }


}
