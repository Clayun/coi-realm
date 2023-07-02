package com.mcylm.coi.realm.item.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.gui.BuildEditGUI;
import com.mcylm.coi.realm.gui.BuilderGUI;
import com.mcylm.coi.realm.item.COICustomItem;
import com.mcylm.coi.realm.item.impl.tools.COIRocket;
import com.mcylm.coi.realm.item.impl.tools.COITownPortal;
import com.mcylm.coi.realm.player.COIPlayer;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.BuildData;
import com.mcylm.coi.realm.tools.selection.Selector;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class COICustomItems {

    public static final COICustomItem BUILDING_BLUEPRINT = new COICustomItem.Builder("building_blueprint", LoggerUtils.replaceColor("&b建筑蓝图"), Material.BOOK)
            .lore(LoggerUtils.replaceColor("&f游戏必不可少的建筑蓝图"),
                    LoggerUtils.replaceColor("&c右键&f使用他建造各类建筑"),
                    LoggerUtils.replaceColor("&f建造需要消耗大量的绿宝石"),
                    LoggerUtils.replaceColor("&b赶紧带上你的兄弟们挖矿吧"))
            .itemDropEvent(event -> event.setCancelled(true))
            .itemUseEvent(event -> {
                if (Action.RIGHT_CLICK_BLOCK == event.getAction() && event.getHand().equals(EquipmentSlot.HAND)
                        //空手触发
                        && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOOK
                ) {

                    if(Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){


                        if(!event.getPlayer().getWorld().getName().equals(Entry.WORLD)){
                            event.setCancelled(true);
                            LoggerUtils.sendMessage("&c当前世界非游戏世界", event.getPlayer());
                            TeamUtils.tpSpawner(event.getPlayer());
                            return;
                        }

                    }

                    Block clickedBlock = event.getClickedBlock();
                    Location location = clickedBlock.getLocation();

                    COIBuilding building = BuildData.getBuildingByBlock(clickedBlock);
                    if (building != null && !building.getType().equals(COIBuildingType.BRIDGE)) {

                        if (building.getTeam() == TeamUtils.getTeamByPlayer(event.getPlayer())) {
                            new BuildEditGUI(event.getPlayer(), building).open();
                        }
                    } else {

                        if(building != null && building.getType().equals(COIBuildingType.BRIDGE)
                                && event.getPlayer().isSneaking()){
                            new BuildEditGUI(event.getPlayer(), building).open();
                        }else{
                            Player player = event.getPlayer();

                            if (Selector.selectors.containsKey(player)) {
                                Selector.selectors.get(player).selectLocation(location);
                            } else {
                                new BuilderGUI(player, location);
                            }
                        }
                    }
                }
            })
            .shopSettings(new COICustomItem.ShopSettings().showInShop(false))
            .build();

    public static final COICustomItem COMMAND_STAR = new COICustomItem.Builder("command_star", LoggerUtils.replaceColor("&b指挥目的地"), Material.NETHER_STAR)
            .lore(LoggerUtils.replaceColor("&f让你的战士向你所指向的位置攻击"))
            .itemDropEvent(event -> event.setCancelled(true))
            .itemUseEvent(event -> {
                if (Action.RIGHT_CLICK_BLOCK == event.getAction() && event.getHand().equals(EquipmentSlot.HAND)
                        //空手触发
                        && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.NETHER_STAR
                ) {
                    COIPlayer coiPlayer = Entry.getGame().getCOIPlayer(event.getPlayer());

                    if(!Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){
                        event.setCancelled(true);
                        LoggerUtils.sendMessage("&c当前世界非游戏世界", event.getPlayer());
                        TeamUtils.tpSpawner(event.getPlayer());
                        return;
                    }
                    Block target = event.getPlayer().getTargetBlockExact(32);
                    if (target == null) {
                        LoggerUtils.sendMessage("&e你所指向的位置太远了", event.getPlayer());
                        return;
                    }
                    coiPlayer.getAttackTeam().setTarget(target.getLocation());

                    for (Location location : LocationUtils.line(target.getLocation(), target.getLocation().add(0,10,0), 0.5)) {
                        location.getWorld().spawnParticle(Particle.DRIP_LAVA, location, 2);
                    }
                }
            })
            .shopSettings(new COICustomItem.ShopSettings().showInShop(false))
            .build();
    public static final List<COICustomItem> DEFAULT_ITEMS = List.of(
            new COICustomItem.Builder("town_portal", "回城卷轴", Material.FLOWER_BANNER_PATTERN)
                    .lore(GUIUtils.autoLineFeed("右键使用开始施法回城,施法过程中" +
                            "10秒内不能受到伤害或者移动," +
                            "否则施法会被打断"))
                    .itemUseEvent(event -> {
                        Action action = event.getAction();

                        //判断是右手，同时避免触发两次
                        if ((Action.RIGHT_CLICK_AIR == action || Action.RIGHT_CLICK_BLOCK == action) && event.getHand().equals(EquipmentSlot.HAND)
                                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.FLOWER_BANNER_PATTERN
                        ) {

                            // 开始使用卷轴
                            // 删除卷轴
                            event.getPlayer().getInventory().setItemInMainHand(null);

                            // 开始施法
                            COITownPortal townPortal = new COITownPortal();
                            townPortal.back(event.getPlayer());
                        }
                    })
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(1)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(1))
                    .build(),

            new COICustomItem.Builder("stone_sword", "石剑", Material.STONE_SWORD)
                    .lore(GUIUtils.autoLineFeed("一把平平无奇的石剑,凑活用吧"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(50)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(1))
                    .build(),
            new COICustomItem.Builder("bow", "弓", Material.BOW)
                    .lore(GUIUtils.autoLineFeed("一把平平无奇的弓,凑活用吧"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(50)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(2))
                    .build(),
            new COICustomItem.Builder("arrow", "箭", Material.ARROW)
                    .lore(GUIUtils.autoLineFeed("箭矢"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(false)
                            .num(64)
                            .price(64)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(1))
                    .build(),

            new COICustomItem.Builder("iron_sword", "铁剑", Material.IRON_SWORD)
                    .lore(GUIUtils.autoLineFeed("锋利的铁剑，工艺上乘！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(100)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(2))
                    .build(),

            new COICustomItem.Builder("diamond_sword", "钻石剑", Material.DIAMOND_SWORD)
                    .lore(GUIUtils.autoLineFeed("钻石制成的剑，削铁如泥！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(200)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(3))
                    .build(),

            new COICustomItem.Builder("leather_helmet", "皮革头盔", Material.LEATHER_HELMET)
                    .lore(GUIUtils.autoLineFeed("平平无奇"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(20)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(2))
                    .build(),

            new COICustomItem.Builder("leather_chestplate", "皮革胸甲", Material.LEATHER_CHESTPLATE)
                    .lore(GUIUtils.autoLineFeed("平平无奇"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(20)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(2))
                    .build(),
            new COICustomItem.Builder("leather_leggings", "皮革护腿", Material.LEATHER_LEGGINGS)
                    .lore(GUIUtils.autoLineFeed("平平无奇"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(20)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(2))
                    .build(),

            new COICustomItem.Builder("leather_boots", "皮革长靴", Material.LEATHER_BOOTS).lore(GUIUtils.autoLineFeed("平平无奇")).shopSettings(new COICustomItem.ShopSettings().showInShop(true).num(1).price(20).buildingType(COIBuildingType.FORGE).buildingsNum(1).buildingLevel(2)).build(),

            new COICustomItem.Builder("iron_helmet", "铁质头盔", Material.IRON_HELMET)
                    .lore(GUIUtils.autoLineFeed("这玩意可是铁的啊，强的很！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(50)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(3))
                    .build(),

            new COICustomItem.Builder("iron_chestplate", "铁质胸甲", Material.IRON_CHESTPLATE)
                    .lore(GUIUtils.autoLineFeed("这玩意可是铁的啊，强的很！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(50)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(3))
                    .build(),

            new COICustomItem.Builder("iron_leggings", "铁质护腿", Material.IRON_LEGGINGS)
                    .lore(GUIUtils.autoLineFeed("这玩意可是铁的啊，强的很！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(50)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(3))
                    .build(),
            new COICustomItem.Builder("iron_boots", "铁质长靴", Material.IRON_BOOTS)
                    .lore(GUIUtils.autoLineFeed("这玩意可是铁的啊，强的很！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(50)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(3))
                    .build(),
            new COICustomItem.Builder("diamond_helmet", "钻石头盔", Material.DIAMOND_HELMET)
                    .lore(GUIUtils.autoLineFeed("钻石套装，童叟无欺！"))
                    .shopSettings(new COICustomItem.ShopSettings().showInShop(true).num(1).price(100).buildingType(COIBuildingType.FORGE).buildingsNum(1).buildingLevel(4)).build(),
            new COICustomItem.Builder("diamond_chestplate", "钻石胸甲", Material.DIAMOND_CHESTPLATE)
                    .lore(GUIUtils.autoLineFeed("钻石套装，童叟无欺！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(100)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(4))
                    .build(),

            new COICustomItem.Builder("diamond_leggings", "钻石护腿", Material.DIAMOND_LEGGINGS)
                    .lore(GUIUtils.autoLineFeed("钻石套装，童叟无欺！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(100)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(4))
                    .build(),

            new COICustomItem.Builder("diamond_boots", "钻石长靴", Material.DIAMOND_BOOTS)
                    .lore(GUIUtils.autoLineFeed("钻石套装，童叟无欺！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(100)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(4))
                    .build(),

            new COICustomItem.Builder("golden_apple", "金苹果", Material.GOLDEN_APPLE)
                    .lore(GUIUtils.autoLineFeed("好吃的！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(100)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(4))
                    .build(),

            new COICustomItem.Builder("enchanted_golden_apple", "附魔金苹果", Material.ENCHANTED_GOLDEN_APPLE)
                    .lore(GUIUtils.autoLineFeed("超级好吃的！"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(200)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(5))
                    .build(),

            new COICustomItem.Builder("snowball", "爆炸果实", Material.SNOWBALL)
                    .lore(GUIUtils.autoLineFeed("射出去后击中的目标会创造一次小型的爆炸，会对敌方建筑产生30点伤害。\n如果击中敌方生物，则会造成3点真实伤害，无视护甲。\n如果击中野怪，直接造成10点真实伤害。"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(4)
                            .price(256)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(1))
                    .build(),

            new COICustomItem.Builder("firework_rocket", "助推器", Material.FIREWORK_ROCKET)
                    .lore(GUIUtils.autoLineFeed("装备鞘翅后，滑翔的过程中使用助推器可以加速飞行，但是小心别把自己摔死了！这玩意可不防摔！！每次消耗8个资源。请务必配合鞘翅使用。"))
                    .itemUseEvent(event -> {
                        Player player = event.getPlayer();
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (item.getType() == Material.FIREWORK_ROCKET) {
                            // 取消发射
                            event.setCancelled(true);
                            if (player.isGliding()) {
                                // 如果玩家正在使用鞘翅，就开启助推机制
                                COIRocket rocket = new COIRocket();
                                rocket.jet(player);
                            }
                        }

                    }).shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(200)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(5))
                    .build(),

            new COICustomItem.Builder("elytra", "鞘翅", Material.ELYTRA)
                    .lore(GUIUtils.autoLineFeed("这是可以帮你在天上滑行的好东西。但是小心别把自己摔死了！这玩意可不防摔！！可以配合喷气动力鞋使用。"))
                    .shopSettings(new COICustomItem.ShopSettings()
                            .showInShop(true)
                            .num(1)
                            .price(200)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(5))
                    .build(),

            BUILDING_BLUEPRINT,
            COMMAND_STAR

    );


}
