package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.RepairTask;
import com.mcylm.coi.realm.runnable.TurretTask;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.utils.GUIUtils;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

@Data
public class COIRepair extends COIBuilding {

    // 攻击敌人可以附加给敌人的负面BUFF，
    // 如果是对己方的增益类塔，可以设置正面BUFF
    private String buff;
    // 粒子效果
    private String particle;
    // 粒子大小
    private int particleSize;
    // 最小攻击伤害
    private double minDamage;
    // 最大攻击伤害
    private double maxDamage;
    // 击退距离
    private double repulsionDistance;
    // 每次攻击的之间的间隔时间（秒）
    private double coolDown;
    // 自动攻击 task
    private RepairTask turretCoolDown;

    // 攻击半径
    private double radius;

    // 弹药库
    private Inventory inventory;

    // 弹药消耗
    private int ammunitionConsumption;

    public COIRepair() {
        // TODO 这块要搬到配置文件里
        // BUFF 是 effect
        this.buff = null;
        // 粒子效果
        this.particle = "REDSTONE";
        // 粒子的大小
        this.particleSize = 1;
        // 最小伤害（回血数量）
        this.minDamage = 2d;
        // 最大伤害
        this.maxDamage = 3d;
        // 击退距离
        this.repulsionDistance = 0d;
        // 每次攻击的间隔时间
        this.coolDown = 4;
        // 攻击半径，如果发射方块和目标之间有其他方块挡着，是不会触发攻击的
        this.radius = 30;
        // 弹药库，如果里面有弹药，才能正常攻击，否则无法攻击
        // 每次攻击消耗 1 颗绿宝石
        // “大炮一响，黄金万两”
        this.inventory = GUIUtils.createAmmoInventory(6);
        // 每次攻击消耗的弹药
        this.ammunitionConsumption = 1;
        // 默认等级为1
        setLevel(1);
        // 初始化NPC创建器
        setNpcCreators(new ArrayList<>());
        //初始化完成，可建造
        setAvailable(true);
        initStructure();
    }


    @Override
    public BuildingConfig getDefaultConfig() {
        return new BuildingConfig()
                .setMaxLevel(3)
                .setMaxBuild(10)
                .setConsume(512)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);
        // 建造完成就开启自动检测周围敌方单位并攻击
        // 自动攻击Task
        turretCoolDown = new RepairTask(this);
        turretCoolDown.action();
    }

    @Override
    public void upgradeBuildSuccess() {
        super.upgradeBuildSuccess();
        // 升级成功
        // 先关闭
        Bukkit.getScheduler().cancelTask(this.getTurretCoolDown().getTaskId());
        // 数据升级
        upgrade();
        // 重启防御塔
        this.turretCoolDown = new RepairTask(this);
        this.turretCoolDown.action();
    }

    @Override
    public void destroy(boolean effect) {
        super.destroy(effect);
        // 拆除成功后，关闭攻击task
        Bukkit.getScheduler().cancelTask(this.getTurretCoolDown().getTaskId());
    }

    /**
     * 初始化设置矿场的建筑等级对照表
     */
    private void initStructure() {
        getBuildingLevelStructure().put(1, "turret1.structure");
        getBuildingLevelStructure().put(2, "turret2.structure");
        getBuildingLevelStructure().put(3, "turret2.structure");
    }

    @Override
    public int getMaxHealth() {
        return 100 + getLevel() * 50;
    }

    /**
     * 生成攻击动画
     * @param enemy
     * @param core
     * @param turret
     */
    public static void animation(Entity enemy, Location core, COIRepair turret){

        double distance = enemy.getLocation().distance(core);

        Vector p1 = enemy.getLocation().toVector();
        p1.setY(p1.getY() + 1.25D);

        Vector p2 = core.toVector();
        Vector vector = p1.clone().subtract(p2).normalize().multiply(0.25D);
        double length = 0.0D;

        Location actual = new Location(core.getWorld(), p2.getX(), p2.getY(), p2.getZ());

        core.getWorld().playSound(core, Sound.valueOf("BLOCK_FIRE_EXTINGUISH"), 1, 1f);

        for (; length < distance; p2.add(vector)) {
            actual = new Location(core.getWorld(), p2.getX(), p2.getY(), p2.getZ());
            generateParticles(turret.getParticle(), actual, 0.0F, 0.0F, 0.0F, Color.BLUE, 1,turret.getParticleSize());
            length += 0.25D;
        }
        Vector nuevoVector = vector.clone().setY(vector.getY() + 0.2D);
        // 击退距离
        nuevoVector.multiply(turret.getRepulsionDistance());
        enemy.setVelocity(nuevoVector);
    }

    /**
     * 直接连线两个BLOCK
     * @param enemy
     * @param core
     * @param turret
     */
    public static void animationBlock(Location enemy, Location core, COIRepair turret){

        double distance = enemy.distance(core);

        Vector p1 = enemy.toVector();
        p1.setY(p1.getY() + 1.25D);

        Vector p2 = core.toVector();
        Vector vector = p1.clone().subtract(p2).normalize().multiply(0.25D);
        double length = 0.0D;

        Location actual = new Location(core.getWorld(), p2.getX(), p2.getY(), p2.getZ());

        core.getWorld().playSound(core, Sound.valueOf("BLOCK_FIRE_EXTINGUISH"), 1, 1f);

        for (; length < distance; p2.add(vector)) {
            actual = new Location(core.getWorld(), p2.getX(), p2.getY(), p2.getZ());
            generateParticles(turret.getParticle(), actual, 0.0F, 0.0F, 0.0F, Color.BLUE, 1,turret.getParticleSize());
            length += 0.25D;
        }
    }

    /**
     * 生成粒子效果
     * @param particle 粒子效果
     * @param loc
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param color 粒子颜色
     * @param count
     * @param size 粒子大小
     */
    public static void generateParticles(String particle, Location loc, float xOffset, float yOffset, float zOffset, Color color, int count,int size) {


        float x = (float)loc.getX();
        float y = (float)loc.getY();
        float z = (float)loc.getZ();

        // 执行一个异步任务
        Bukkit.getScheduler().runTaskAsynchronously(Entry.getInstance(), new Runnable() {
            @Override
            public void run() {
                // 在这里执行需要耗时操作的代码
                Particle.DustOptions options = new Particle.DustOptions(color,2);
                loc.getWorld().spawnParticle(Particle.valueOf(particle),x,y,z,count,xOffset,yOffset,zOffset,0d,options);
            }
        });

    }

    private void upgrade(){
        // 最小伤害
        this.minDamage = this.minDamage + getLevel();
        // 最大伤害
        this.maxDamage = this.maxDamage + getLevel();
        // 每次攻击的间隔时间
        this.coolDown = this.coolDown - 1;
        // 弹药消耗增大
        this.ammunitionConsumption = this.ammunitionConsumption + 1;
    }
}
