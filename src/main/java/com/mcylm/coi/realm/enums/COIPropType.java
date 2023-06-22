package com.mcylm.coi.realm.enums;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.AllArgsConstructor;
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

    TOWN_PORTAL(
            "TOWN_PORTAL",
            "回城卷轴",
            new ItemStack(Material.FLOWER_BANNER_PATTERN),
            1,
            "右键使用开始施法回城,施法过程中" +
                    "10秒内不能受到伤害或者移动," +
                    "否则施法会被打断",
            1,
            COIBuildingType.FORGE,
            1,
            1
    ),

    // 武器系列
    STONE_SWORD(
            "STONE_SWORD",
            "石剑",
            new ItemStack(Material.STONE_SWORD),
            1,
            "一把平平无奇的石剑,凑活用吧",
            50,
            COIBuildingType.FORGE,
            1,
            1
    ),

    BOW(
            "BOW",
            "弓",
            new ItemStack(Material.BOW),
            1,
            "一把平平无奇的弓,凑活用吧",
            50,
            COIBuildingType.FORGE,
            1,
            2
    ),

    CROSSBOW(
            "CROSSBOW",
            "弩",
            new ItemStack(Material.CROSSBOW),
            1,
            "一把平平无奇的弩,凑活用吧",
            50,
            COIBuildingType.FORGE,
            1,
            2
    ),

    ARROW(
            "ARROW",
            "箭",
            new ItemStack(Material.ARROW),
            64,
            "箭矢",
            64,
            COIBuildingType.FORGE,
            1,
            1
    ),

    IRON_SWORD(
            "IRON_SWORD",
            "铁剑",
            new ItemStack(Material.IRON_SWORD),
            1,
            "锋利的铁剑，工艺上乘！",
            100,
            COIBuildingType.FORGE,
            1,
            2
    ),

    DIAMOND_SWORD(
            "DIAMOND_SWORD",
            "钻石剑",
            new ItemStack(Material.DIAMOND_SWORD),
            1,
            "钻石制成的剑，削铁如泥！",
            200,
            COIBuildingType.FORGE,
            1,
            3
    ),


    // 皮革套装系列
    LEATHER_HELMET(
            "LEATHER_HELMET",
            "皮革头盔",
            new ItemStack(Material.LEATHER_HELMET),
            1,
            "平平无奇",
            20,
            COIBuildingType.FORGE,
            1,
            2
    ),

    LEATHER_CHESTPLATE(
            "LEATHER_CHESTPLATE",
            "皮革胸甲",
            new ItemStack(Material.LEATHER_CHESTPLATE),
            1,
            "平平无奇",
            20,
            COIBuildingType.FORGE,
            1,
            2
    ),

    LEATHER_LEGGINGS(
            "LEATHER_LEGGINGS",
            "皮革护腿",
            new ItemStack(Material.LEATHER_LEGGINGS),
            1,
            "平平无奇",
            20,
            COIBuildingType.FORGE,
            1,
            2
    ),

    LEATHER_BOOTS(
            "LEATHER_BOOTS",
            "皮革长靴",
            new ItemStack(Material.LEATHER_BOOTS),
            1,
            "平平无奇",
            20,
            COIBuildingType.FORGE,
            1,
            2
    ),

    // 铁套装系列
    IRON_HELMET(
            "IRON_HELMET",
            "铁质头盔",
            new ItemStack(Material.IRON_HELMET),
            1,
            "这玩意可是铁的啊，强的很！",
            50,
            COIBuildingType.FORGE,
            1,
            3
    ),

    IRON_CHESTPLATE(
            "IRON_CHESTPLATE",
            "铁质胸甲",
            new ItemStack(Material.IRON_CHESTPLATE),
            1,
            "这玩意可是铁的啊，强的很！",
            50,
            COIBuildingType.FORGE,
            1,
            3
    ),

    IRON_LEGGINGS(
            "IRON_LEGGINGS",
            "铁质护腿",
            new ItemStack(Material.IRON_LEGGINGS),
            1,
            "这玩意可是铁的啊，强的很！",
            50,
            COIBuildingType.FORGE,
            1,
            3
    ),

    IRON_BOOTS(
            "IRON_BOOTS",
            "铁质长靴",
            new ItemStack(Material.IRON_BOOTS),
            1,
            "这玩意可是铁的啊，强的很！",
            50,
            COIBuildingType.FORGE,
            1,
            3
    ),

    // 钻石套装系列
    DIAMOND_HELMET(
            "DIAMOND_HELMET",
            "钻石头盔",
            new ItemStack(Material.DIAMOND_HELMET),
            1,
            "钻石套装，童叟无欺！",
            100,
            COIBuildingType.FORGE,
            1,
            4
    ),

    DIAMOND_CHESTPLATE(
            "DIAMOND_CHESTPLATE",
            "钻石胸甲",
            new ItemStack(Material.DIAMOND_CHESTPLATE),
            1,
            "钻石套装，童叟无欺！",
            100,
            COIBuildingType.FORGE,
            1,
            4
    ),

    DIAMOND_LEGGINGS(
            "DIAMOND_LEGGINGS",
            "钻石护腿",
            new ItemStack(Material.DIAMOND_LEGGINGS),
            1,
            "钻石套装，童叟无欺！",
            100,
            COIBuildingType.FORGE,
            1,
            4
    ),

    DIAMOND_BOOTS(
            "DIAMOND_BOOTS",
            "钻石长靴",
            new ItemStack(Material.DIAMOND_BOOTS),
            1,
            "钻石套装，童叟无欺！",
            100,
            COIBuildingType.FORGE,
            1,
            4
    ),

    // 道具
    GOLDEN_APPLE(
            "GOLDEN_APPLE",
            "金苹果",
            new ItemStack(Material.GOLDEN_APPLE),
            1,
            "好吃的！",
            100,
            COIBuildingType.FORGE,
            1,
            4
    ),

    /*
    ENCHANTED_GOLDEN_APPLE(
            "ENCHANTED_GOLDEN_APPLE",
            "附魔金苹果",
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            1,
            "超级好吃的！",
            200,
            COIBuildingType.FORGE,
            1,
            5
    ),

     */

    SNOWBALL(
            "SNOWBALL",
            "爆炸果实",
            new ItemStack(Material.SNOWBALL),
            4,
            "射出去后击中的目标会" +
                    "创造一次小型的爆炸,会对敌方建筑产生30点伤害," +
                    "如果击中敌方生物，则会造成3点真实伤害，无视护甲",
            256,
            COIBuildingType.FORGE,
            1,
            1
    ),

    FIREWORK_ROCKET(
            "FIREWORK_ROCKET",
            "助推器",
            new ItemStack(Material.FIREWORK_ROCKET),
            1,
            "装备鞘翅后，滑翔的过程中" +
                    "使用助推器可以加速飞行" +
                    "但是小心别把自己摔死了！" +
                    "这玩意可不防摔！！" +
                    "每次消耗8个资源" +
                    "请务必配合*鞘翅*使用。",
            2000,
            COIBuildingType.FORGE,
            1,
            5
    ),

    ELYTRA(
            "ELYTRA",
            "鞘翅",
            new ItemStack(Material.ELYTRA),
            1,
            "这是可以帮你在天上滑行的好东西。" +
                    "但是小心别把自己摔死了！" +
                    "这玩意可不防摔！！" +
                    "可以配合*喷气动力鞋*使用。",
            2000,
            COIBuildingType.FORGE,
            1,
            5
    ),


    ;


    // CODE
    private String code;
    // 名称 name
    private String name;
    // GUI显示的材质
    private ItemStack itemType;
    // 数量
    private int num;
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
    public static boolean checkUnlock(COITeam team, COIPropType type){

        // 是否开启解锁功能
        boolean openLock = Entry.getInstance().getConfig().getBoolean("game.building.lock");

        if(!openLock){
            return true;
        }

        COIPropType[] values = COIPropType.values();

        for(COIPropType unlockType : values){

            // 匹配建筑类型
            if(unlockType.equals(type)){
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
