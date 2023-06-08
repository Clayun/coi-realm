package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.npc.AI;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * AI属性
 */
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class COINpc implements Serializable {

    // ID
    private String id;

    // npc名称
    private String name;

    // 等级
    private int level;

    // 所属小队
    private COITeam team;

    // 所属建筑
    private COIBuilding building;

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

    // NPC的素材收集背包
    private Inventory inventory;

    // 警戒范围
    private double alertRadius = 5;

    // 攻击伤害最小值
    private double minDamage = 2;

    // 攻击伤害最大值
    private double maxDamage = 8;

    // 跟随的玩家
    // 很重要的属性，决定了NPC产生的积分结算给哪个玩家
    private String followPlayerName;

    // 是否死亡复活
    // 如果是一次性的NPC，建议设置为false
    // 死亡时自动删除
    private boolean canRespawn = true;

    // 出生点
    private Location spawnLocation;

    // 重生所需时间
    private Integer respawnDelay = 30;

    // NPC
    private AI npc;

    // NPC Type
    protected EntityType npcType = EntityType.PLAYER;

    // 伪装成其他实体
    private DisguiseType disguiseType;

    // 伪装时对flag watcher 进行的操作
    private Consumer<FlagWatcher> flagWatcherHandler;

    // 服装类型
    public static List<Material> CLOTHES = new ArrayList<>(){{
        // 头盔
        add(Material.CHAINMAIL_HELMET);
        add(Material.DIAMOND_HELMET);
        add(Material.GOLDEN_HELMET);
        add(Material.IRON_HELMET);
        add(Material.LEATHER_HELMET);
        add(Material.NETHERITE_HELMET);
        add(Material.TURTLE_HELMET);

        // 胸甲
        add(Material.CHAINMAIL_CHESTPLATE);
        add(Material.DIAMOND_CHESTPLATE);
        add(Material.GOLDEN_CHESTPLATE);
        add(Material.IRON_CHESTPLATE);
        add(Material.LEATHER_CHESTPLATE);
        add(Material.NETHERITE_CHESTPLATE);

        // 裤子
        add(Material.LEATHER_LEGGINGS);
        add(Material.CHAINMAIL_LEGGINGS);
        add(Material.DIAMOND_LEGGINGS);
        add(Material.GOLDEN_LEGGINGS);
        add(Material.IRON_LEGGINGS);
        add(Material.NETHERITE_LEGGINGS);

        // 鞋子
        add(Material.CHAINMAIL_BOOTS);
        add(Material.DIAMOND_BOOTS);
        add(Material.GOLDEN_BOOTS);
        add(Material.IRON_BOOTS);
        add(Material.LEATHER_BOOTS);
        add(Material.NETHERITE_BOOTS);
    }};

    public void remove() {
        setCanRespawn(false);
        npc.despawn();
        npc.remove();

    }

    public void upgrade() {
        level++;
    }

    /**
     * 获取NPC数据
     * @param entity
     * @return
     */
    public static COINpc getNPCByEntity(Entity entity){
        @Nullable COINpc data = EntityData.getNpcByEntity(entity);

        if (data != null) {
            return data;
        }

        return null;
    }
}
