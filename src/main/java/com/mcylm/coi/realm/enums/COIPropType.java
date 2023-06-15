package com.mcylm.coi.realm.enums;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.item.COIJetpack;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * 道具购买类型
 *
 * TODO 放到配置文件里面
 */
@Getter
@AllArgsConstructor
public enum COIPropType {


    JET_PACK(
            "JET_PACK",
            "空气动力鞋",
            new ItemStack(Material.CHAINMAIL_BOOTS),
            "这是可以帮你起飞的好东西。" +
                    "但是小心别把自己摔死了！" +
                    "这玩意可不防摔！！" +
                    "每次使用消耗8个资源，" +
                    "请务必配合*鞘翅*使用。",
            2000,
            COIBuildingType.FORGE,
            1,
            1
    ),

    ELYTRA(
            "ELYTRA",
            "鞘翅",
            new ItemStack(Material.ELYTRA),
            "这是可以帮你在天上滑行的好东西。" +
                    "但是小心别把自己摔死了！" +
                    "这玩意可不防摔！！" +
                    "可以配合*喷气动力鞋*使用。",
            2000,
            COIBuildingType.FORGE,
            1,
            1
    ),

    // 武器系列
    STONE_SWORD(
            "STONE_SWORD",
            "石剑",
            new ItemStack(Material.STONE_SWORD),
            "一把平平无奇的石剑,凑活用吧",
            500,
            COIBuildingType.FORGE,
            1,
            1
    ),

    IRON_SWORD(
            "IRON_SWORD",
            "铁剑",
            new ItemStack(Material.IRON_SWORD),
            "锋利的铁剑，工艺上乘！",
            1000,
            COIBuildingType.FORGE,
            1,
            2
    ),

    DIAMOND_SWORD(
            "DIAMOND_SWORD",
            "钻石剑",
            new ItemStack(Material.DIAMOND_SWORD),
            "钻石制成的剑，削铁如泥！",
            2000,
            COIBuildingType.FORGE,
            1,
            3
    ),


    // 皮革套装系列
    LEATHER_HELMET(
            "LEATHER_HELMET",
            "皮革头盔",
            new ItemStack(Material.LEATHER_HELMET),
            "平平无奇",
            500,
            COIBuildingType.FORGE,
            1,
            1
    ),

    LEATHER_CHESTPLATE(
            "LEATHER_CHESTPLATE",
            "皮革胸甲",
            new ItemStack(Material.LEATHER_CHESTPLATE),
            "平平无奇",
            500,
            COIBuildingType.FORGE,
            1,
            1
    ),

    LEATHER_LEGGINGS(
            "LEATHER_LEGGINGS",
            "皮革护腿",
            new ItemStack(Material.LEATHER_LEGGINGS),
            "平平无奇",
            500,
            COIBuildingType.FORGE,
            1,
            1
    ),

    LEATHER_BOOTS(
            "LEATHER_BOOTS",
            "皮革长靴",
            new ItemStack(Material.LEATHER_BOOTS),
            "平平无奇",
            500,
            COIBuildingType.FORGE,
            1,
            1
    ),

    // 铁套装系列
    IRON_HELMET(
            "IRON_HELMET",
            "铁质头盔",
            new ItemStack(Material.IRON_HELMET),
            "这玩意可是铁的啊，强的很！",
            1000,
            COIBuildingType.FORGE,
            1,
            2
    ),

    IRON_CHESTPLATE(
            "IRON_CHESTPLATE",
            "铁质胸甲",
            new ItemStack(Material.IRON_CHESTPLATE),
            "这玩意可是铁的啊，强的很！",
            1000,
            COIBuildingType.FORGE,
            1,
            2
    ),

    IRON_LEGGINGS(
            "IRON_LEGGINGS",
            "铁质护腿",
            new ItemStack(Material.IRON_LEGGINGS),
            "这玩意可是铁的啊，强的很！",
            1000,
            COIBuildingType.FORGE,
            1,
            2
    ),

    IRON_BOOTS(
            "IRON_BOOTS",
            "铁质长靴",
            new ItemStack(Material.IRON_BOOTS),
            "这玩意可是铁的啊，强的很！",
            1000,
            COIBuildingType.FORGE,
            1,
            2
    ),

    // 钻石套装系列
    DIAMOND_HELMET(
            "DIAMOND_HELMET",
            "钻石头盔",
            new ItemStack(Material.DIAMOND_HELMET),
            "钻石套装，童叟无欺！",
            2000,
            COIBuildingType.FORGE,
            1,
            3
    ),

    DIAMOND_CHESTPLATE(
            "DIAMOND_CHESTPLATE",
            "钻石胸甲",
            new ItemStack(Material.DIAMOND_CHESTPLATE),
            "钻石套装，童叟无欺！",
            2000,
            COIBuildingType.FORGE,
            1,
            3
    ),

    DIAMOND_LEGGINGS(
            "DIAMOND_LEGGINGS",
            "钻石护腿",
            new ItemStack(Material.DIAMOND_LEGGINGS),
            "钻石套装，童叟无欺！",
            2000,
            COIBuildingType.FORGE,
            1,
            3
    ),

    DIAMOND_BOOTS(
            "DIAMOND_BOOTS",
            "钻石长靴",
            new ItemStack(Material.DIAMOND_BOOTS),
            "钻石套装，童叟无欺！",
            2000,
            COIBuildingType.FORGE,
            1,
            3
    ),

    // 道具
    GOLDEN_APPLE(
            "GOLDEN_APPLE",
            "金苹果",
            new ItemStack(Material.GOLDEN_APPLE),
            "好吃的！",
            1000,
            COIBuildingType.FORGE,
            1,
            3
    ),

    ENCHANTED_GOLDEN_APPLE(
            "ENCHANTED_GOLDEN_APPLE",
            "附魔金苹果",
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            "超级好吃的！",
            2000,
            COIBuildingType.FORGE,
            1,
            4
    ),

    ;


    // CODE
    private String code;
    // 名称 name
    private String name;
    // GUI显示的材质
    private ItemStack itemType;
    // 介绍
    private String introduce;
    // 价格
    private int price;

    // 解锁相关的设置
    // 建筑类型
    private COIBuildingType buildingType;
    // 建筑数量
    private int buildingsNum;
    // 前置建筑满足的等级
    private int buildingLevel;

    /**
     * 获取全部道具列表
     * @return
     */
    public static List<COIPropType> getProps(){
        return Arrays.stream(COIPropType.values()).toList();
    }

    /**
     * 自动检查当前小队是否满足解锁条件
     * @param team
     * @param type
     * @return
     */
    public static boolean checkUnlock(COITeam team, COIBuildingType type){

        // 是否开启解锁功能
        boolean openLock = Entry.getInstance().getConfig().getBoolean("game.building.lock");

        if(!openLock){
            return true;
        }

        COIPropType[] values = COIPropType.values();

        for(COIPropType unlockType : values){

            // 匹配建筑类型
            if(unlockType.getBuildingType().equals(type)){
                // 匹配到了
                // 1.检查前置建筑类型是否为null
                if(unlockType.getBuildingType() == null){
                    // 直接解锁
                    return true;
                }

                // 没有限制，直接解锁
                if(unlockType.getBuildingsNum() == 0
                        && unlockType.getBuildingLevel() == 0){
                    return true;
                }

                // 2.判断当前小队是否已经解锁前置建筑
                // 前置建筑建造的数量
                int perBuildingCount = 0;
                int maxLevel = 0;
                List<COIBuilding> finishedBuildings = team.getFinishedBuildings();

                for(COIBuilding building : finishedBuildings){
                    // 如果匹配到前置建筑
                    if(building.getType().equals(unlockType.getBuildingType())){
                        perBuildingCount ++;
                        if(building.getLevel() > maxLevel){
                            maxLevel = building.getLevel();
                        }
                    }
                }

                // 如果两个条件同时满足
                if(perBuildingCount >= unlockType.getBuildingsNum()
                        && maxLevel >= unlockType.getBuildingLevel()){
                    return true;
                }

                // 未在上面满足条件，未解锁
                return false;
            }
        }

        // 在本枚举类里没有配置，则默认解锁
        return true;
    }
}
