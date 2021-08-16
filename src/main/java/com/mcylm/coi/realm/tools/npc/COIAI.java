package com.mcylm.coi.realm.tools.npc;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

/**
 * AI的接口层，AI的基础能力
 */
public interface COIAI {

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

    // 移动
    void move();

    // 生成NPC
    void spawn(Location location);

    // 消除NPC
    void despawn();

    // 是否还或者
    boolean isAlive();

    // 获取NPC位置
    Location getLocation();
}
