package com.mcylm.coi.realm.tools.building.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.AirRaidTask;
import com.mcylm.coi.realm.runnable.TurretTask;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.config.BuildingConfig;
import com.mcylm.coi.realm.utils.GUIUtils;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;

@Data
public class COIAirRaid extends COIBuilding {

    // 最小攻击伤害
    private double minDamage;
    // 最大攻击伤害
    private double maxDamage;
    // 每次攻击的之间的间隔时间（秒）
    private double coolDown;
    // 自动攻击 task
    private AirRaidTask turretCoolDown;

    // 攻击半径
    private double radius;

    // 弹药库
    private Inventory inventory;

    // 弹药消耗
    private int ammunitionConsumption;

    // 炮口位置
    private Location muzzle;

    public COIAirRaid() {
        // 最小伤害
        this.minDamage = 1d;
        // 最大伤害
        this.maxDamage = 3d;
        // 每次攻击的间隔时间
        this.coolDown = 3;
        // 攻击半径，如果发射方块和目标之间有其他方块挡着，是不会触发攻击的
        this.radius = 50;
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
                .setMaxLevel(10)
                .setMaxBuild(30)
                .setConsume(512)
                .setStructures(getBuildingLevelStructure());
    }

    @Override
    public void buildSuccess(Location location, Player player) {
        super.buildSuccess(location, player);
        // 建造完成就开启自动检测周围敌方单位并攻击
        // 自动攻击Task
        this.turretCoolDown = new AirRaidTask(this);
        this.turretCoolDown.action();

    }

    @Override
    public void upgradeBuildSuccess() {
        // 升级成功
        super.upgradeBuildSuccess();
        // 先关闭
        Bukkit.getScheduler().cancelTask(this.getTurretCoolDown().getTaskId());
        // 数据升级
        upgrade();
        // 重启防御塔
        this.turretCoolDown = new AirRaidTask(this);
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
        getBuildingLevelStructure().put(4, "turret2.structure");
        getBuildingLevelStructure().put(5, "turret2.structure");
        getBuildingLevelStructure().put(6, "turret2.structure");
        getBuildingLevelStructure().put(7, "turret2.structure");
        getBuildingLevelStructure().put(8, "turret2.structure");
        getBuildingLevelStructure().put(9, "turret2.structure");
        getBuildingLevelStructure().put(10, "turret2.structure");
    }

    @Override
    public int getMaxHealth() {
        return 100 + getLevel() * 100;
    }



    /**
     * 升级
     */
    private void upgrade(){
        // 最小伤害
        this.minDamage = this.minDamage + 1;
        // 最大伤害
        this.maxDamage = this.maxDamage + 2;
        // 每次攻击的间隔时间
        if(this.coolDown > 1){
            this.coolDown = this.coolDown - 1;
        }

        // 弹药消耗增大
        if(this.ammunitionConsumption < 5){
            this.ammunitionConsumption = this.ammunitionConsumption + 1;
        }
    }
}
