package com.mcylm.coi.realm.runnable;


import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.impl.COIAirRaid;
import com.mcylm.coi.realm.tools.building.impl.COITurret;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * 防空系统
 */
public class AirRaidTask {

    private int taskId;
    private COIAirRaid turret;

    // 炮口位置
    private Location muzzle;

    public AirRaidTask(COIAirRaid turret) {
        this.turret = turret;
    }

    public int getTaskId() {
        return taskId;
    }

    public void action(){

        BukkitScheduler sh = Bukkit.getServer().getScheduler();
        this.taskId = sh.scheduleSyncRepeatingTask(Entry.getInstance(), new Runnable() {
                    public void run() {
                        attack();
                    }
                }, 0L, getTicks());
    }

    protected void attack() {

        if(this.muzzle == null){
            LoggerUtils.debug("正在检测防空炮口位置");
            String material = Entry.getInstance().getConfig().getString("game.turret.muzzle-material");
            Location muzzleLoc = this.turret.getBlockLocationByMaterial(material);
            this.muzzle = muzzleLoc;
        }

        Location l = this.muzzle.clone();
        // 从方块的正中心点作为防空判定
        l.setY(l.getY() + 0.5D);
        l.setX(l.getX() + 0.5D);
        l.setZ(l.getZ() + 0.5D);
        List<Entity> enemy = getNearestEnemy(l, this.turret);
        if (!enemy.isEmpty()) {

            // 扣除玩家背包里的资源
            boolean b = deductionResources(this.turret.getAmmunitionConsumption());

            if (!b) {
                LoggerUtils.sendActionbar(Bukkit.getPlayer(this.turret.getBuildPlayerName()),"防御塔没弹药了，无法攻击当前入侵者！请尽快补充弹药");
                return;
            }

            for(Entity entity : enemy){
                double minDamage = this.turret.getMinDamage() * 100.0D;
                double maxDamage = this.turret.getMaxDamage() * 100.0D;
                double realDamage = getNumeroAleatorio((int)minDamage, (int)maxDamage) / 100.0D;

                animation(entity);
                ((LivingEntity)entity).damage(realDamage);
                ((LivingEntity)entity).setNoDamageTicks(0);
            }


        }
    }

    /**
     * 生成闪电的动画
     * @param entity
     */
    private void animation(Entity entity){
        entity.getWorld().strikeLightningEffect(entity.getLocation());
        this.muzzle.getWorld().strikeLightningEffect(this.muzzle);
    }

    /**
     * 获取最近的敌人
     * @param lOriginal
     * @param torreta
     * @return
     */
    private static List<Entity> getNearestEnemy(Location lOriginal, COIAirRaid torreta) {
        double radio = torreta.getRadius();
        Collection<Entity> entities = lOriginal.getWorld().getNearbyEntities(lOriginal, radio, radio, radio);

        List<Entity> entitiesList = new ArrayList<>();

        for (Entity e : entities) {
            if (e != null && !e.isDead()) {
                // 攻击权限
                boolean attackPermission = false;
                if (e.getType().equals(EntityType.PLAYER)) {
                    Player p = (Player)e;

                    // 飞行中的玩家锁定
                    if(p.isGliding() || p.isFlying()){
                        // 先将实体当作玩家判断是否是本小队的
                        // 同时将观察者剔除
                        if(TeamUtils.getTeamByPlayer(p) != torreta.getTeam()
                                && p.getGameMode() != GameMode.SPECTATOR){
                            // 非小队内成员，同时非所属人
                            // 就设置为攻击目标
                            attackPermission = true;
                        }
                    }
                }


                if (attackPermission) {
                    // 如果是攻击目标，就开打
                    entitiesList.add(e);
                }
            }

        }

        return entitiesList;
    }

    public static double getNumeroAleatorio(int min, int max) {
        Random r = new Random();
        return r.nextInt(max - min + 1) + min;
    }

    private long getTicks() { return (long)(this.turret.getCoolDown() * 20.0D); }

    public boolean deductionResources(int amount) {

        String materialName = Entry.getInstance().getConfig().getString("game.building.material");
        int playerHadResource = ItemUtils.getItemAmountFromInventory(this.turret.getInventory(),Material.getMaterial(materialName));

        // 如果玩家手里的资源数量足够
        if (playerHadResource >= amount) {

            // 扣减物品
            ItemStack[] contents =
                    this.turret.getInventory().getContents();

            // 剩余所需扣减资源数量
            int deductionCount = amount;

            // 资源类型
            Material material = Material.getMaterial(materialName);
            for (ItemStack itemStack : contents) {

                if (itemStack == null) {
                    continue;
                }

                // 是资源物品才扣减
                if (itemStack.getType().equals(material)) {
                    // 如果当前物品的堆叠数量大于所需资源，就只扣减数量
                    if (itemStack.getAmount() > deductionCount) {
                        itemStack.setAmount(itemStack.getAmount() - deductionCount);
                        return true;
                    }

                    // 如果当前物品的堆叠数量等于所需资源，就删物品
                    if (itemStack.getAmount() == deductionCount) {
                        this.turret.getInventory().removeItem(itemStack);
                        return true;
                    }

                    // 如果物品的堆叠数量小于所需资源，就删物品，同时计数
                    if (itemStack.getAmount() < deductionCount) {
                        // 减去当前物品的库存
                        deductionCount = deductionCount - itemStack.getAmount();
                        this.turret.getInventory().removeItem(itemStack);
                    }
                }


            }

        } else
            return false;

        return false;
    }

}
