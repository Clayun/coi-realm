package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.Events;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class GameListener implements Listener {


    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){

        Player p = event.getPlayer();
        COITeam team = TeamUtils.getTeamByPlayer(p);
        if(team !=null){
            event.setRespawnLocation(team.getSpawner());
            p.teleport(team.getSpawner());
        }
    }

    @EventHandler
    public void onEntityRespawn(EntitySpawnEvent event){

        if(!CitizensAPI.getNPCRegistry().isNPC(event.getEntity())){

            if(event.getEntity() instanceof LivingEntity){

                // 先取消生成
                event.setCancelled(true);

                // 周围30格有建筑物则不允许生成怪物小队
                // TODO 游戏中后期要自动调整这个数字
//                int radius = 40;
//
//                // 必须在周围有玩家才能生成
//                int playerRadius = 20;
//
//                int maxMonsterBase = 50;
//
//                // 生成的几率
//                boolean result = hasChance(0.03);
//
//                if(result){
//                    // 判断所在位置附近有没有建筑物
//                    Location clone = event.getLocation().clone();
//
//                    for (Block b : LocationUtils.selectionRadiusByDistance(clone.getBlock(), radius, radius)) {
//                        COIBuilding building = BuildData.getBuildingByBlock(b);
//                        if (building != null && building.getTeam() != TeamUtils.getMonsterTeam()) {
//
//                            // 半径范围内发现玩家小队的建筑，禁止生成
//                            return;
//                        }
//                    }
//
//                    Collection<LivingEntity> nearbyEntities = clone.getNearbyLivingEntities(playerRadius, playerRadius, playerRadius);
//
//                    // 周围必须有玩家才能生成
//                    boolean hasPlayer = false;
//                    for(LivingEntity entity : nearbyEntities){
//                        if(entity instanceof Player){
//                            hasPlayer = true;
//                            break;
//                        }
//                    }
//
//                    if(!hasPlayer){
//                        return;
//                    }
//
//                    COITeam monsterTeam = Entry.getGame().getMonsterTeam();
//
//                    if(monsterTeam != null){
//                        // 开启怪物阵营的情况下，才会生成
//
//                        if(monsterTeam.getFinishedBuildings().size() >= maxMonsterBase){
//                            // 超过最大创建数量了，禁止生成
//                            return;
//                        }
//
//                        clone.setY(clone.getY() - 1);
//                        COIBuilding monsterBase = null;
//                        try {
//                            monsterBase = Entry.getInstance().getBuildingManager().getBuildingTemplateByType(COIBuildingType.MONSTER_BASE);
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                        monsterBase.setTeam(monsterTeam);
//                        monsterBase.build(clone,monsterTeam,false);
//                    }
//                }


            }


        }
    }

    /**
     * 百分之多少的几率返回true
     * @param chance 1 = 100%,0.1 = 10%
     * @return
     */
    private boolean hasChance(double chance) {
        return Math.random() < chance;
    }
}
