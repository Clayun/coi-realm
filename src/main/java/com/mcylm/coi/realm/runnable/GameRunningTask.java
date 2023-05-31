package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameRunningTask {

    // 等待时间
    private static Integer gamingTimer = Entry.getInstance().getConfig().getInt("game.gaming-timer");

    public static void runTask() {

        // 游戏状态标为游戏中
        Entry.getGame().setStatus(COIGameStatus.GAMING);

        // 游戏中进程
        // 1.开启倒计时
        // 2.游戏结束后启动 GameStoppingTask
        new BukkitRunnable() {

            int count = 0;

            @Override
            public void run() {
                count ++;

                // 倒计时的秒数
                Integer countdown = gamingTimer - count;

                if(count >= gamingTimer){
                    // 启动结算进程
                    GameStoppingTask.runTask();

                    // 结束当前进程
                    this.cancel();

                }else{

                    // boss bar 的进度条
                    float progress = countdown.floatValue() / gamingTimer.floatValue() * 100;

                    // 游戏在进行中，倒计时需要在 boss bar 中展示
                    for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                        p.showBossBar(BossBar.bossBar(
                                Component.text("还剩"+countdown+"秒结束游戏"),
                                progress,
                                BossBar.Color.PINK,
                                BossBar.Overlay.NOTCHED_6));
                    }

                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 1);


    }

}
