package com.mcylm.coi.realm.item.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 回城卷轴
 */
public class COITownPortal {


    // 施法秒数
    @Getter
    private int counting = 10;

    /**
     * 回城
     * @param p
     */
    public void back(Player p){

        COITeam teamByPlayer = TeamUtils.getTeamByPlayer(p);

        if(teamByPlayer == null
            || teamByPlayer.isDefeat()){
            LoggerUtils.sendActionbar(p,"&c回城失败，小队不存在！");
            return;
        }

        new BukkitRunnable(){

            // 血量缓存
            double initialHealth = p.getHealth();
            // 缓存的位置
            Location initialLocation = p.getLocation();
            int count = 0;
            @Override
            public void run() {

                if(count == counting){


                    Entry.runSync(new BukkitRunnable(){

                        @Override
                        public void run() {
                            p.teleport(teamByPlayer.getSpawner());
                        }
                    });

                    cancel();
                }

                // 实时血量
                double currentHealth = p.getHealth();

                if(currentHealth < initialHealth){
                    LoggerUtils.sendActionbar(p,"&c回城过程中受到伤害被打断施法");
                    cancel();
                }else{
                    int countDown = counting - count;
                    LoggerUtils.sendActionbar(p,"&b回城施法中...还剩 &c"+countDown+" &b秒");
                }

                Location location = p.getLocation();

                if(location.distance(initialLocation) > 1){
                    // 移动距离超出允许的范围
                    LoggerUtils.sendActionbar(p,"&c回城过程中移动被取消施法");
                    cancel();
                }

                count++;
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(),0,20);
    }

}
