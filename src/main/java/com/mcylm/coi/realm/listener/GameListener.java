package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.Events;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

                // 是个活得
                if(event.getEntity().getType().equals(EntityType.ZOMBIE)
                ){
                    // 非CitizensAPI生成的指定上述类型的原版怪物
                    // 创建一只普通怪
//                    try {
//
//                        COITeam monsterTeam = Entry.getGame().getMonsterTeam();
//
//                        if(monsterTeam != null){
//                            // 怪物阵营存在的情况下，才会生成
//
//                            COIBuilding monsterBase = Entry.getInstance().getBuildingManager().getBuildingTemplateByType(COIBuildingType.MONSTER_BASE);
//                            monsterBase.setTeam(monsterTeam);
//                            monsterBase.build(event.getLocation(),monsterTeam,false);
//                        }
//
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
                }


            }


        }
    }
}
