package com.mcylm.coi.realm.tools.goals.citizens;

import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.data.metadata.AttackTeamData;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class NPCFollowTeamBehavior extends BehaviorGoalAdapter {

    private Mob entity;

    private COIEntity coiEntity;

    private LivingEntity followingEntity;

    private AttackTeam team;

    // 保持饥饿度到多少
    private int keepHunger = 20;

    // 每次投喂多少个
    private int feedNum = 2;

    // 2秒扣一次，避免给旋了
    private int skipFeedAction = 0;

    // 最大的半径范围
    private int maxRadius = 30;

    private int tick = 0;

    public NPCFollowTeamBehavior(COIEntity entity) {
        this.coiEntity = entity;
        this.entity = (Mob) entity.getNpc().getEntity();

    }

    /*
    @Override
    public void start() {
        LoggerUtils.debug("start follow");
    }

    @Override
    public void stop() {
        LoggerUtils.debug("stop follow");
    }

    @Override
    public boolean shouldActivate() {

    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {

        int index = team.getMembers().indexOf(coiEntity);
        if (index == -1) {
            return;
        }
        if (index == 0) {
            followingEntity = team.getCommander();
        } else {
            for (int i = index; i >= 0 ; i--) {
                COIEntity member = team.getMembers().get(i);

                if (member.isAlive() && member != coiEntity) {
                    AttackTeamData memberData = AttackTeamData.getDataByEntity(member.getNpc().getEntity());
                    if (memberData.getStatus() == AttackTeamData.Status.FOLLOWING) {
                        followingEntity = (LivingEntity) team.getMembers().get(i).getNpc().getEntity();

                        if (entity.getLocation().distance(followingEntity.getLocation()) > 1.5) {
                            LoggerUtils.debug("try move");
                            entity.getPathfinder().findPath(followingEntity);
                        }

                    }
                    break;
                }
            }
        }

        if (coiEntity.isAlive() && coiEntity.getHunger() < keepHunger) {
            if (team.getCommander() instanceof Player player) {

                if (skipFeedAction == 40) {
                    skipFeedAction = 0;
                }

                if (skipFeedAction == 0) {
                    @NotNull HashMap<Integer, ItemStack> extra = player.getInventory().removeItem(new ItemStack(Material.BREAD, feedNum));
                    if (extra.isEmpty()) {
                        coiEntity.addItemToInventory(new ItemStack(Material.BREAD, feedNum));
                    } else {
                        extra.values().forEach(item -> {
                            if (feedNum - item.getAmount() > 0) {
                                coiEntity.addItemToInventory(new ItemStack(Material.BREAD, feedNum - item.getAmount()));
                            }
                        });
                    }
                }

                skipFeedAction++;

            }
        }

    }



    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return GoalKey.of(Mob.class, Entry.getNamespacedKey("npc_follow_team"));
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }

     */

    @Override
    public void reset() {

    }

    @Override
    public BehaviorStatus run() {

        if (tick++ > 5) {
            tick = 0;
        } else {
            return BehaviorStatus.RUNNING;
        }

        int index = team.getMembers().indexOf(coiEntity);

        if (index == -1) {
            return BehaviorStatus.FAILURE;
        }
        if (index == 0) {

            followingEntity = team.getCommander();
        } else {
            for (int i = index; i >= 0 ; i--) {
                COIEntity member = team.getMembers().get(i);

                if (member.isAlive() && member != coiEntity) {
                    AttackTeamData memberData = AttackTeamData.getDataByEntity(member.getNpc().getEntity());
                    if (memberData.getStatus() == AttackTeamData.Status.FOLLOWING) {
                        followingEntity = (LivingEntity) team.getMembers().get(i).getNpc().getEntity();

                    }
                    break;
                }
            }
        }

        if (coiEntity.isAlive() && coiEntity.getHunger() < keepHunger) {
            if (team.getCommander() instanceof Player player) {

                if (skipFeedAction == 40) {
                    skipFeedAction = 0;
                }

                if (skipFeedAction == 0) {
                    @NotNull HashMap<Integer, ItemStack> extra = player.getInventory().removeItem(new ItemStack(Material.BREAD, feedNum));
                    if (extra.isEmpty()) {
                        coiEntity.addItemToInventory(new ItemStack(Material.BREAD, feedNum));
                    } else {
                        extra.values().forEach(item -> {
                            if (feedNum - item.getAmount() > 0) {
                                coiEntity.addItemToInventory(new ItemStack(Material.BREAD, feedNum - item.getAmount()));
                            }
                        });
                    }
                }

                skipFeedAction++;

            }
        }

        if (!followingEntity.isDead()) {
            if (entity.getLocation().distance(followingEntity.getLocation()) > 1.5) {
                coiEntity.findPath(followingEntity.getLocation());
            }
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {

        if (!coiEntity.isAlive()) {
            return false;
        }
        this.entity = (Mob) coiEntity.getNpc().getEntity();


        AttackTeamData teamData = AttackTeamData.getDataByEntity(entity);

        if (teamData == null) {
            return false;
        }

        team = teamData.getCurrentTeam();

        if (team.getCommander().isDead()) {
            return false;
        }

        if (teamData.getStatus() == AttackTeamData.Status.OTHER) {
            return false;
        }
        if (coiEntity.isTooHungryToWork()) {
            return false;
        }

        double distance = entity.getLocation().distance(teamData.getCurrentTeam().getCommander().getLocation());

        if (distance > maxRadius) {
            if (distance > maxRadius * 1.4) {
                return false;
            }
            return true;
        }

        if (coiEntity instanceof COISoldier soldier) {
            return soldier.getTarget() == null || soldier.getTarget().isDead();
        }

        return true;
    }


}
