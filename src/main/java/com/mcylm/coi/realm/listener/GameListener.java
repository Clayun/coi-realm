package com.mcylm.coi.realm.listener;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.clipboard.PlayerClipboard;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIServerMode;
import com.mcylm.coi.realm.events.BuildingDamagedEvent;
import com.mcylm.coi.realm.events.BuildingDestroyedEvent;
import com.mcylm.coi.realm.events.BuildingTouchEvent;
import com.mcylm.coi.realm.player.COIPlayer;
import com.mcylm.coi.realm.tools.attack.target.impl.BuildingTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.impl.COIRepair;
import com.mcylm.coi.realm.tools.building.impl.COITurret;
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
import net.kyori.adventure.util.Ticks;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

                String message = "&c注意，您的 &6"+event.getBuilding().getType().getName()+" &c正在被 "+attacker+" 攻击！";

                // 基地被攻击
                if(event.getBuilding().getType().equals(COIBuildingType.BASE)){
                    // Title提醒
                    Title title = Title.title(

                            Component.text(LoggerUtils.replaceColor("&c注意！")),
                            Component.text(LoggerUtils.replaceColor("&f"+event.getBuilding().getType().getName()+"正在被 "+attacker+" &f攻击，快防守！")),
                            Title.DEFAULT_TIMES);
                    p.showTitle(title);
                    // 普通消息
                    LoggerUtils.sendActionbar(LoggerUtils.replaceColor(message),p);
                }else{
                    // 普通建筑被攻击
                    p.sendActionBar(Component.text(LoggerUtils.replaceColor(message)));
//                    LoggerUtils.sendMessage(LoggerUtils.replaceColor(message),p);
                }


            }

        }

        // 处理防御塔掉落子弹
        int drops = 0;
        COIBuilding building = event.getBuilding();

        if(building instanceof COITurret turret){
            int i = ItemUtils.deductionResources(drops, turret.getInventory());
            if(i > 0){
                String materialName = Entry.getInstance().getConfig().getString("game.building.material");

                // 掉落的东西
                ItemStack itemStack = new ItemStack(Material.getMaterial(materialName));
                itemStack.setAmount(i);

                event.getAttackedBlock().getLocation().getWorld()
                        .dropItem(event.getAttackedBlock().getLocation(),itemStack);
            }

        }else if(building instanceof COIRepair turret){
            int i = ItemUtils.deductionResources(drops, turret.getInventory());
            if(i > 0){
                String materialName = Entry.getInstance().getConfig().getString("game.building.material");

                // 掉落的东西
                ItemStack itemStack = new ItemStack(Material.getMaterial(materialName));
                itemStack.setAmount(i);

                event.getAttackedBlock().getLocation().getWorld()
                        .dropItem(event.getAttackedBlock().getLocation(),itemStack);
            }
        }

    }

    @EventHandler
    public void onBuildingDestroyed(BuildingDestroyedEvent event){


        COITeam team = event.getBuilding().getTeam();
        for (String playerName : team.getPlayers()) {

            Player p = Bukkit.getPlayer(playerName);

            if(p != null && p.isOnline()){

                String message = "&c注意，您的 &6"+event.getBuilding().getType().getName()+" &c已被拆除！";

                // 基地被攻击
                if(event.getBuilding().getType().equals(COIBuildingType.BASE)){
                    // Title提醒
                    Title title = Title.title(

                            Component.text(LoggerUtils.replaceColor("&c注意！")),
                            Component.text(LoggerUtils.replaceColor("&f您队伍的 &6"+event.getBuilding().getType().getName()+" &c已被拆除！")),
                            Title.DEFAULT_TIMES);
                    p.showTitle(title);
                    // 普通消息
                    LoggerUtils.sendMessage(LoggerUtils.replaceColor(message),p);

                }else{
                    // 普通建筑被攻击
                    p.sendActionBar(Component.text(LoggerUtils.replaceColor(message)));
                    LoggerUtils.sendMessage(LoggerUtils.replaceColor(message),p);
                }


            }

        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){

        Player player = event.getPlayer();

        Block block = event.getBlock();
        COIBuilding building = BuildData.getBuildingByBlock(block);
        if (building != null && building.getTeam() != TeamUtils.getTeamByPlayer(player)) {
            building.damage(player,10,block);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){

        Player p = event.getPlayer();
        COITeam team = TeamUtils.getTeamByPlayer(p);
        if(team !=null){

            COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(event.getPlayer());

            if(coiPlayer.isDeath()){

                waitDeath(event.getPlayer());

            }else{
                event.setRespawnLocation(team.getSpawner());
                p.teleport(team.getSpawner());
            }



        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){

        COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(event.getPlayer());

        if(coiPlayer.isDeath()){
            event.setCancelled(true);
        }

    }

    private void waitDeath(Player p){
        COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(p);

        if(coiPlayer.isDeath()){
            // 获取死亡倒计时
            int resurrectionCountdown = coiPlayer.getResurrectionCountdown();

            // 传送到地狱小黑屋
            Location clone = p.getLocation().clone();
            clone.setY(20);
            p.setNoDamageTicks(20 * (resurrectionCountdown + 1));
            p.teleport(clone);

            new BukkitRunnable(){

                int count = 0;
                @Override
                public void run() {
                    count++;
                    if(count == resurrectionCountdown){

                        // 传送回出生点
                        COITeam team = TeamUtils.getTeamByPlayer(p);
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                p.teleport(team.getSpawner());
                            }
                        }.runTask(Entry.getInstance());
                        // 取消无敌
                        p.setNoDamageTicks(0);
                        coiPlayer.setDeath(false);
                        cancel();
                    }else{
                        int countDown = resurrectionCountdown - count;

                        // TITLE
                        Title.Times times = Title.Times.times(Ticks.duration(0L), Ticks.duration(70L), Ticks.duration(0L));

                        Title title = Title.title(
                                Component.text(LoggerUtils.replaceColor("&f"+countDown+" &c马上复活！")),
                                Component.text(LoggerUtils.replaceColor("&f失败乃兵家常事，少侠不必气馁！")),
                                times);

                        if(p.isOnline()){
                            p.showTitle(title);
                        }else{
                            // 离线了就关闭进程
                            cancel();
                        }

                    }



                }
            }.runTaskTimerAsynchronously(Entry.getInstance(),0,20);

        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){

        Player p = event.getPlayer();
        COITeam team = TeamUtils.getTeamByPlayer(p);

        // 游戏中进来的话，就要初始化信息了
        if(team == null && Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){
            // 自动加入队伍
            TeamUtils.autoJoinTeam(p);
            // 传送到小队复活点
            TeamUtils.tpSpawner(p);
            // 初始化玩家背包
            Entry.getGame().initPlayerGaming(p);
        }else{
            // 玩家已经加入了小队，要判断是否复活了
            // 死亡记录
            COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(event.getPlayer());

            if(coiPlayer.isDeath()){
                // 等待死亡
                waitDeath(event.getPlayer());
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

        if (item.getType() == Material.IRON_PICKAXE
                || item.getType() == Material.DIAMOND_PICKAXE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBuildingTouch(BuildingTouchEvent event) {

        COIBuilding building = event.getBuilding();
        building.displayHealth(event.getPlayer());

        if(building.getType().equals(COIBuildingType.TURRET_NORMAL)
                || building.getType().equals(COIBuildingType.TURRET_REPAIR)
        ){
            // 如果是塔类型，就打开该塔的弹药库GUI
            // 仅限本小队才允许
            if(building.getTeam() == TeamUtils.getTeamByPlayer(event.getPlayer())){
                if(building instanceof COITurret turret){
                    event.getPlayer().openInventory(turret.getInventory());
                }else if(building instanceof COIRepair turret){
                    event.getPlayer().openInventory(turret.getInventory());
                }
            }

        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        String material = Entry.getInstance().getConfig().getString("game.building.material");

        // 仅掉落绿宝石，其他的都保留

        List<ItemStack> needSave = new ArrayList<>();

        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while(iterator.hasNext()){
            ItemStack item = iterator.next();

            if (item.getType() != Material.getMaterial(material)) {
                needSave.add(item);
                event.getItemsToKeep().add(item);
            }
        }

        // 清空
        event.getDrops().removeAll(needSave);

        // 死亡记录
        COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(event.getPlayer());
        coiPlayer.setDeathCount(coiPlayer.getDeathCount() + 1);
        coiPlayer.setDeath(true);
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
