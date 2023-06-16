package com.mcylm.coi.realm.item;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIPropType;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import com.mcylm.coi.realm.utils.DamageUtils;
import com.mcylm.coi.realm.utils.InventoryUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

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


    public static void registerListener() {



        Events.subscribe(PlayerInteractEvent.class).handler(event -> {

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

        });
    }
}
