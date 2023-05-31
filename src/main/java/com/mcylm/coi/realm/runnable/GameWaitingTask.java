package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;

public class GameWaitingTask {

    // 等待时间
    private static Integer waitingTimer = Entry.getInstance().getConfig().getInt("game.waiting-timer");

    public static void runTask() {

        // 游戏状态标志为等待中
        Entry.getGame().setStatus(COIGameStatus.WAITING);

        // TODO 可以优化一下显示内容，加入 Bossbar 显示一些实时动态数据等
        // 游戏开始进程
        // 1.倒计时
        // 2.倒计时结束，没有选队伍的玩家自动选一个队伍
        // 4.将玩家全部传送到队伍的默认出生点
        // 5.启动 GameRunningTask
        new BukkitRunnable() {

            int count = 0;

            @Override
            public void run() {

                // 判断人数是否满足最小开始游戏的人数
                int size = Entry.getInstance().getServer().getOnlinePlayers().size();
                // 最少开启游戏人数
                int minPlayers = Entry.getInstance().getConfig().getInt("game.min-players");


                if(size >= minPlayers){
                    // 大于或等于最小在线人数
                    // 倒计时开始
                    count++;

                    if(count >= waitingTimer){
                        // 倒计时完成
                        // 获取全部没有选择队伍的玩家，按顺序自动匹配进去
                        autoJoinTeam();

                        // 全部玩家传送到默认出生点
                        TeamUtils.tpAllPlayersToSpawner();

                        // 开始下一个游戏中进程
                        GameRunningTask.runTask();

                        // 关闭当前task
                        this.cancel();

                    }else{

                        // 倒计时的秒数
                        int countdown = waitingTimer - count;

                        // 展示倒计时信息
                        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                            Title title = Title.title(

                                    // todo 颜色调整，放入配置文件中配置，倒计时秒数变成配置变量
                                    Component.text(countdown+"秒等待中..."),
                                    Component.text("使用手中的钻石镐来选择队伍吧"),
                                    Title.DEFAULT_TIMES);
                            p.showTitle(title);
                        }
                    }
                }else{
                    // 不满足最小人数，倒计时重置
                    count = 0;
                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 1);


    }

    /**
     * 自动将玩家匹配一个队伍
     */
    private static void autoJoinTeam(){

        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

            COITeam team = TeamUtils.getTeamByPlayer(p);

            if(team == null){
                // 玩家没选择队伍
                // 自动选择一个人数最少的队伍丢进去
                COITeam minPlayersTeam = TeamUtils.getMinPlayersTeam();

                if(minPlayersTeam == null){
                    // 全都满了，直接给当前玩家踢了吧
                    p.kick(Component.text("当前服务器已满，请更换服务器后重试"), PlayerKickEvent.Cause.KICK_COMMAND);
                    return;
                }

                // 替玩家加入小队
                minPlayersTeam.join(p);

            }
        }

    }

}
