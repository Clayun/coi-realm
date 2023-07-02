package com.mcylm.coi.realm.item.impl;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.item.COICustomItem;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.InventoryUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 鞘翅推进器
 * 利用击退的原理来实现助推效果
 */
public class COIRocket {

    // 每次消耗资源
    @Getter
    private int cost = 8;
    // 每次飞行的距离
    @Getter
    private double distance = 1;
    @Getter
    private double springPower = 2;

    @Getter
    private static final COICustomItem item = new COICustomItem.Builder( "rocket", "助推器", Material.FIREWORK_ROCKET)
                .lore(GUIUtils.autoLineFeed("装备鞘翅后，滑翔的过程中" +
                        "使用助推器可以加速飞行" +
                        "但是小心别把自己摔死了！" +
                        "这玩意可不防摔！！" +
                        "每次消耗8个资源" +
                        "请务必配合*鞘翅*使用。"))
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
                            .showInShop(false)
                            .num(1)
                            .price(200)
                            .buildingType(COIBuildingType.FORGE)
                            .buildingsNum(1)
                            .buildingLevel(5)
                    ).build();

    // 喷气
    public boolean jet(Player p){

        boolean b = InventoryUtils.deductionResources(p, cost);

        if(!b){
            LoggerUtils.sendActionbar(p,"&c背包内资源不足，无法起飞");
            return false;
        }
        // 击退距离
        Location location = p.getLocation();
        Location eyeLocation = p.getEyeLocation();
        p.setVelocity(eyeLocation.getDirection().multiply(getSpringPower()).setY(getDistance()));
        p.playSound(location, Sound.ENTITY_BAT_TAKEOFF, 10, 0);
        p.getWorld().spawnParticle(Particle.CRIT, location, 15);

        return true;
    }


}
