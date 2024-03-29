package com.mcylm.coi.realm.item.impl.tools;

import com.mcylm.coi.realm.utils.InventoryUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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
