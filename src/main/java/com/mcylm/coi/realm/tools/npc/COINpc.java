package com.mcylm.coi.realm.tools.npc;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * AI属性
 */
@Data
public class COINpc implements Serializable {

    // ID
    private String id;

    // npc名称
    private String name;

    // 等级
    private int level;

    // 血量
    private double health;

    // 皮肤相关参数
    private String skinTextures;
    private String skinName;
    private String skinSignature;

    // 攻击状态 true 主动攻击 false 被动攻击，默认 false
    private boolean aggressive = false;

    // NPC的友军（玩家名称）
    private Set<String> friendPlayers;

    // NPC的敌对玩家（玩家名称）
    private Set<String> enemyPlayers;

    // 敌对生物
    private Set<EntityType> enemyEntities;

    // 会主动拆除的方块
    private Set<String> breakBlockMaterials;

    // 主动捡起的物品类型
    private Set<String> pickItemMaterials;

    // NPC的背包
    private List<ItemStack> inventory;

    // 警戒范围
    private double alertRadius = 5;

    // 攻击伤害最小值
    private double minDamage = 1;

    // 攻击伤害最大值
    private double maxDamage = 3;

    // 跟随的玩家
    private String followPlayerName;

    // 是否死亡复活
    private boolean canRespawn;

    // 出生点
    private Location spawnLocation;
}
