package com.mcylm.coi.realm.runnable;


import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.impl.COITurret;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;

public class TurretTask {

    private int taskId;
    private COITurret turret;

    // 炮口位置
    private Location muzzle;

    public TurretTask(COITurret turret) {
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
            LoggerUtils.debug("正在检测炮口位置");
            String material = Entry.getInstance().getConfig().getString("game.turret.muzzle-material");
            Location muzzleLoc = this.turret.getBlockLocationByMaterial(material);
            this.muzzle = muzzleLoc;
        }

        Location l = this.muzzle.clone();
        // 从方块的正中心点射出
        l.setY(l.getY() + 0.5D);
        l.setX(l.getX() + 0.5D);
        l.setZ(l.getZ() + 0.5D);
        Entity enemy = getNearestEnemy(l, this.turret);
        if (enemy != null) {

            // 扣除玩家背包里的资源
            boolean b = deductionResources(this.turret.getAmmunitionConsumption());

            if (!b) {
                LoggerUtils.sendActionbar(Bukkit.getPlayer(this.turret.getBuildPlayerName()),"防御塔没弹药了，无法攻击当前入侵者！请尽快补充弹药");
                return;
            }

            double minDamage = this.turret.getMinDamage() * 100.0D;
            double maxDamage = this.turret.getMaxDamage() * 100.0D;
            double realDamage = getNumeroAleatorio((int)minDamage, (int)maxDamage) / 100.0D;

            turret.animation(enemy, l,this.turret);
//            enemigo.setMetadata("TurretDamage", new FixedMetadataValue(Entry.getInstance(), String.valueOf(this.turret.getOwner()) + ";" + this.turret.getTipo()));
            ((LivingEntity)enemy).damage(realDamage);
            ((LivingEntity)enemy).setNoDamageTicks(0);
            if(StringUtils.isNotBlank(this.turret.getBuff())){

                String buff = this.turret.getBuff();

                String[] buffConfig = buff.split(";");
                if(buffConfig.length == 3){

                    String type = buffConfig[0];
                    Integer duration = Integer.valueOf(buffConfig[1]);
                    Integer amplifier = Integer.valueOf(buffConfig[2]);

                    PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(type),duration,amplifier);
                    ((Player) enemy).addPotionEffect(potionEffect);
                }
            }

//            Player player = (Player) enemigo;
//            LoggerUtils.sendMessage("&6受到&b"+this.turret.getName()+"&c攻击造成 &b"+realDamage+" &c点真实伤害",player);
        }
    }

    /**
     * 获取最近的敌人
     * @param lOriginal
     * @param torreta
     * @return
     */
    private static Entity getNearestEnemy(Location lOriginal, COITurret torreta) {
        double radio = torreta.getRadius();
        Entity closest = null;
        double closestDist = 100.0D;
        Collection<Entity> entities = lOriginal.getWorld().getNearbyEntities(lOriginal, radio, radio, radio);

        for (Entity e : entities) {
            if (e != null && !e.isDead()) {
                // 攻击权限
                boolean attackPermission = false;
                if (e.getType().equals(EntityType.PLAYER)) {
                    Player p = (Player)e;

                    // 先将实体当作玩家判断是否是本小队的
                    if(TeamUtils.getTeamByPlayer(p) != torreta.getTeam()){
                        // 非小队内成员，同时非所属人
                        // 就设置为攻击目标
                        attackPermission = true;
                    }

                    if(attackPermission){
                        // 如果实体作为玩家非本校对，就把他再当作NPC去判断
                        if(TeamUtils.checkNPCInTeam(e,torreta.getTeam())){
                            // 是本小队的NPC，就取消锁定攻击
                            attackPermission = false;
                            LoggerUtils.debug(e.getName()+"是本小队的NPC，取消锁定攻击");
                        }
                    }

                }

                if (attackPermission) {
                    // 如果是攻击目标，就开打
                    Location l = e.getLocation();
                    if (l.distance(lOriginal) < closestDist &&
                            !lockTarget(l, lOriginal)) {
                        closest = e;
                        closestDist = l.distance(lOriginal);
                    }
                }
            }
        }

        return closest;
    }

    /**
     * 锁定目标
     * @param enemy
     * @param turret
     * @return
     */
    private static boolean lockTarget(Location enemy, Location turret) {
        double distance = enemy.distance(turret);
        Vector p1 = enemy.toVector(); p1.setY(p1.getY() + 1.25D);
        Vector p2 = turret.toVector();
        Vector vector = p1.clone().subtract(p2).normalize();
        double length = 0.0D;
        Location l = turret.clone();
        for (int i = 0; i < distance; i++) {

            Location actual = l.add(vector);
            Block b = actual.getBlock();
            if (length >= 2.0D && b != null && !b.getType().equals(Material.AIR) && !b.getType().name().contains("LONG_GRASS") && !b.getType().name().contains("TALL_GRASS")) {
                // 玩家是否躲在掩体后面，如果躲在掩体后面就不攻击
                return true;
            }
            length++;
        }
        return false;
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
