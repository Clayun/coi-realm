package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameStoppingTask {

    // 等待时间
    private static Integer stoppingTimer = Entry.getInstance().getConfig().getInt("game.stopping-timer");

    public static void runTask() {

        // 游戏状态标注为结算中
        Entry.getGame().setStatus(COIGameStatus.STOPPING);

        // 游戏结算进程
        // 1.倒计时开始，
        // 2.游戏状态标为结束中（COIGameStatus.STOPPING）
        // 3.结算奖励
        // 4.倒计时结束后重置服务器
        new BukkitRunnable() {

            int count = 0;

            @Override
            public void run() {

                // TODO 结算奖励

                count ++;

                // 倒计时的秒数
                int countdown = stoppingTimer - count;

                for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                    Title title = Title.title(

                            // todo 颜色调整，放入配置文件中配置，倒计时秒数变成配置变量
                            Component.text(countdown+"秒后游戏结束..."),
                            Component.text("奖励已结算，可以在左下角查看"),
                            Title.DEFAULT_TIMES);
                    p.showTitle(title);
                }

                if(count >= stoppingTimer){
                    // TODO 重置当前服务器
                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 1);


    }

}
