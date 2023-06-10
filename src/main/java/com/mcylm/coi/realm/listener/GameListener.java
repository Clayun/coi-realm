package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.events.BuildingDamagedEvent;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.Events;
import net.citizensnpcs.api.CitizensAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class GameListener implements Listener {

    @EventHandler
    public void onBuildingDamaged(BuildingDamagedEvent event){

        String attacker = "神秘生物";

        COITeam npcTeam = TeamUtils.getNPCTeam(event.getEntity());

        if(npcTeam == null){
            npcTeam = TeamUtils.getTeamByPlayer((Player)event.getEntity());
        }

        if(npcTeam != null){
            attacker = npcTeam.getType().getColor()+npcTeam.getType().getName();
        }

        COITeam team = event.getBuilding().getTeam();
        for (String playerName : team.getPlayers()) {

            Player p = Bukkit.getPlayer(playerName);

            if(p != null && p.isOnline()){
                // 基地被攻击
                if(event.getBuilding().getType().equals(COIBuildingType.BASE)){
                    Title title = Title.title(

                            Component.text(LoggerUtils.replaceColor("&c注意！")),
                            Component.text(LoggerUtils.replaceColor("&f基地正在被 "+attacker+" &f攻击，快防守！")),
                            Title.DEFAULT_TIMES);
                    p.showTitle(title);
                }else{
                    // 普通建筑被攻击
                    p.sendActionBar(Component.text(LoggerUtils.replaceColor("&c注意，您的 "+event.getBuilding().getType().getName()+" 正在被"+attacker+"攻击！")));
                }


            }

        }

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){

        Player p = event.getPlayer();
        COITeam team = TeamUtils.getTeamByPlayer(p);
        if(team !=null){
            event.setRespawnLocation(team.getSpawner());
            p.teleport(team.getSpawner());

            // 来一根铁镐子
            if(Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){
                ItemStack ironPickaxe = new ItemStack(Material.IRON_PICKAXE);
                p.getInventory().addItem(ironPickaxe);
            }

        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getType() == Material.BOOK
            && ItemUtils.getName(item).equals(LoggerUtils.replaceColor("&b建筑蓝图"))) {
            event.setCancelled(true);
        }

        if (item.getType() == Material.COMPASS
                && ItemUtils.getName(item).equals(LoggerUtils.replaceColor("&c选择队伍"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BOOK
                    && ItemUtils.getName(item).equals(LoggerUtils.replaceColor("&b建筑蓝图"))) {
                event.getDrops().remove(item);
            }

            if (item != null && item.getType() == Material.COMPASS
                    && ItemUtils.getName(item).equals(LoggerUtils.replaceColor("&c选择队伍"))) {
                event.getDrops().remove(item);
            }
        }
    }

    @EventHandler
    public void onEntityRespawn(EntitySpawnEvent event){

        if(!CitizensAPI.getNPCRegistry().isNPC(event.getEntity())){

            if(event.getEntity() instanceof LivingEntity){
                // 先取消生成
                event.setCancelled(true);
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
