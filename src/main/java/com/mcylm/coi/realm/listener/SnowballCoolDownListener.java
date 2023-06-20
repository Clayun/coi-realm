package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

public class SnowballCoolDownListener implements Listener {
    private HashMap<Player, Long> coolDowns = new HashMap<>();
    private long coolDownTime = 2000; // 2秒CD时间

    @EventHandler
    public void onSnowballLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            // 仅限雪球
            if(event.getEntity().getType().equals(EntityType.SNOWBALL)){
                if (coolDowns.containsKey(player)) {
                    long lastThrowTime = coolDowns.get(player);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastThrowTime < coolDownTime) {
                        event.setCancelled(true); // 取消扔雪球事件

                        LoggerUtils.sendActionbar(player,"&c你需要等待 &b" + (coolDownTime - (currentTime - lastThrowTime)) / 1000 + " &c秒后才能再次发射！");
                    } else {
                        coolDowns.put(player, currentTime);
                    }
                } else {
                    coolDowns.put(player, System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent e) {


        // 基础伤害30点
        Double damage = 30d;

        // 每分钟增加1点
        LocalDateTime startTime = Entry.getGame().getStartTime();
        if(startTime != null){
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(startTime, now);
            long minutes = duration.toMinutes();
            damage = damage + minutes;
        }

        if(e.getEntity().getType().equals(EntityType.SNOWBALL)){
            // 如果是雪球，就判断是否是玩家射出的
            if(e.getEntity().getShooter() instanceof Player player){

                COITeam team = TeamUtils.getTeamByPlayer(player);
                if(team != null && Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){
                    // 玩家在小队里面 同时游戏在进行中

                    if(e.getHitEntity() != null){

                        if(e.getHitEntity() instanceof LivingEntity){
                            COITeam npcTeam = TeamUtils.getNPCTeam(e.getHitEntity());

                            if(npcTeam != null && npcTeam != team){
                                // 如果不是己方的NPC，就创造个爆炸
                                Location location = e.getHitEntity().getLocation();
                                // TODO 这里可以几率触发是否燃烧
                                location.createExplosion(e.getHitEntity(), 3,false, false);
                                // 给到实体3点伤害
                                ((LivingEntity)e.getHitEntity()).damage(3);
                            }

                            if(npcTeam != null && npcTeam == team){
                                // 同队伍禁止
                                e.setCancelled(true);
                            }

                            if(e.getHitEntity() instanceof Player){
                                COITeam teamByPlayer = TeamUtils.getTeamByPlayer((Player) e.getHitEntity());

                                if(teamByPlayer != null && teamByPlayer != team){
                                    // 如果不是己方的玩家，就创造个爆炸
                                    Location location = e.getHitEntity().getLocation();
                                    // TODO 这里可以几率触发是否燃烧
                                    location.createExplosion(e.getHitEntity(), 3,false, false);
                                    // 直接给3伤害
                                    ((LivingEntity)e.getHitEntity()).damage(3);
                                }

                                if(teamByPlayer != null && teamByPlayer == team){
                                    // 同队伍禁止
                                    e.setCancelled(true);
                                }
                            }

                        }

                    }else if(e.getHitBlock() != null){

                        COIBuilding buildingByBlock = BuildData.getBuildingByBlock(e.getHitBlock());

                        if(buildingByBlock != null){

                            if(buildingByBlock.getTeam() != team){
                                // 如果不是己方的建筑，就创造个爆炸
                                e.getHitBlock().getLocation().createExplosion(3,false, false);
                                // 创造30点伤害
                                buildingByBlock.damage(player,damage.intValue(),e.getHitBlock());
                                LoggerUtils.sendActionbar(player,"&b攻击 "
                                        +buildingByBlock.getTeam().getType().getColor()
                                        +buildingByBlock.getTeam().getType().getName()+" "
                                        +buildingByBlock.getType().getName()+" &cx"+damage+" &b点伤害");

                            }
                        }

                    }
                }
            }
        }
    }
}
