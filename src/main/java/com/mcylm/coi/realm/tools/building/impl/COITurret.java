package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.TurretTask;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.utils.ItemUtils;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;

@Data
public class COITurret extends COIBuilding {

    // 攻击敌人可以附加给敌人的负面BUFF，
    // 如果是对己方的增益类塔，可以设置正面BUFF
    private String buff;
    // 粒子效果
    private String particle;
    // 最小攻击伤害
    private double minDamage;
    // 最大攻击伤害
    private double maxDamage;
    // 击退距离
    private double repulsionDistance;
    // 每次攻击的之间的间隔时间（秒）
    private double coolDown;
    // 自动攻击 task
    private TurretTask turretCoolDown;
    private double radius;

    public COITurret() {
        this.buff = null;
        this.particle = "REDSTONE";
        this.minDamage = 3d;
        this.maxDamage = 6d;
        this.coolDown = 2;
        this.turretCoolDown = new TurretTask(this);
        this.radius = 30;
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
                .setMaxLevel(2)
                .setConsume(64)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void buildSuccess(Location location, Player player) {

        // 建造完成就开启自动检测周围敌方单位并攻击
        this.turretCoolDown.action();

    }

    @Override
    public void upgradeBuildSuccess() {

        // 升级成功
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
    public static void animation(Entity enemy, Location core, COITurret turret){

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
            generateParticles(turret.getParticle(), actual, 0.0F, 0.0F, 0.0F, 0.0F, 1);
            length += 0.25D;
        }
        Vector nuevoVector = vector.clone().setY(vector.getY() + 0.2D);
        // 击退距离
        nuevoVector.multiply(turret.getRepulsionDistance());
        enemy.setVelocity(nuevoVector);
    }

    /**
     * 生成粒子效果
     * @param particle
     * @param loc
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param speed
     * @param count
     */
    public static void generateParticles(String particle, Location loc, float xOffset, float yOffset, float zOffset, float speed, int count) {


        float x = (float)loc.getX();
        float y = (float)loc.getY();
        float z = (float)loc.getZ();

        // 执行一个异步任务
        Bukkit.getScheduler().runTaskAsynchronously(Entry.getInstance(), new Runnable() {
            @Override
            public void run() {
                // 在这里执行需要耗时操作的代码
                Particle.DustOptions options = new Particle.DustOptions(Color.BLUE,2);
                loc.getWorld().spawnParticle(Particle.valueOf(particle),x,y,z,count,xOffset,yOffset,zOffset,0d,options);
            }
        });



//        EnumParticle enumParticle = EnumParticle.valueOf(particle);
//
//
//        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(enumParticle, false, x, y, z, xOffset,
//                yOffset, zOffset, speed, count, null);
//
//        for (Player p : Bukkit.getOnlinePlayers()) {
//            (((CraftPlayer)p).getHandle()).playerConnection.sendPacket(packet);
//        }
    }
}
