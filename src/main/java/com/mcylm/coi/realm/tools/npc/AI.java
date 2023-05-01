package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.npc.impl.COIHuman;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * AI的接口层，AI的基础能力
 */
public interface AI {

    // 创建NPC
    COIHuman create(COINpc coiNpc);

    // 更新NPC
    COIHuman update(COINpc coiNpc,boolean respawn);

    // AI寻路
    void findPath(Location location);

    // 说话
    void say(String message);

    // 捡起地上的东西
    void pickItems();

    // 吃东西
    void eatFood();

    // 自动穿衣服
    void wearClothes();

    // 获取NPC附近的方块
    List<Block> getNearbyBlocks(double radius);

    // 获取NPC附近的生物
    List<Entity> getNearByEntities(double radius);

    // 移动
    void move();

    // 生成NPC
    void spawn(Location location);

    // 消除NPC
    void despawn();

    // 彻底清除NPC
    void remove();

    // 是否还或者
    boolean isAlive();

    // 是否被清除
    boolean isRemoved();

    // 获取NPC位置
    Location getLocation();

    default int delayTick() {
        return 20;
    }
}
