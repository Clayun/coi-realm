package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.model.COIPlayerScore;
import com.mcylm.coi.realm.model.COIScoreDetail;
import com.mcylm.coi.realm.runnable.api.GameTaskApi;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BasicGameTask implements GameTaskApi {

    // 等待时间
    private static Integer waitingTimer = Entry.getInstance().getConfig().getInt("game.waiting-timer");
    private static Integer gamingTimer = Entry.getInstance().getConfig().getInt("game.gaming-timer");
    private static Integer stoppingTimer = Entry.getInstance().getConfig().getInt("game.stopping-timer");

    @Override
    public void waiting() {
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
                        TeamUtils.autoJoinTeam();

                        // 全部玩家传送到默认出生点
                        TeamUtils.tpAllPlayersToSpawner();

                        // 开始下一个游戏中进程
                        gaming();

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
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 20);


    }

    @Override
    public void gaming() {
        // 游戏状态标为游戏中
        Entry.getGame().setStatus(COIGameStatus.GAMING);
        // 生成矿脉
        VeinGenerateTask.runTask();
        // 游戏中进程
        // 1.开启倒计时
        // 2.游戏结束后启动 GameStoppingTask
        new BukkitRunnable() {

            int count = 0;

            BossBar bossBar = BossBar.bossBar(
                    Component.text("还剩" + gamingTimer + "秒结束游戏"),
                    1,
                    BossBar.Color.PINK,
                    BossBar.Overlay.NOTCHED_6);

            @Override
            public void run() {
                count ++;

                if(count >= gamingTimer){

                    // 隐藏 boss bar
                    for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                        p.hideBossBar(bossBar);
                    }

                    // 启动结算进程
                    stopping();

                    // 结束当前进程
                    this.cancel();

                }else{

                    // 倒计时的秒数
                    Integer countdown = gamingTimer - count;

                    // boss bar 的进度条
                    float progress = countdown.floatValue() / gamingTimer.floatValue();
                    bossBar.name(Component.text("还剩" + countdown + "秒结束游戏"));
                    bossBar.progress(progress);

                    // 游戏在进行中，倒计时需要在 boss bar 中展示
                    for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                        p.showBossBar(bossBar);
                    }

                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 20);


    }

    @Override
    public void stopping() {
        // 游戏状态标注为结算中
        Entry.getGame().setStatus(COIGameStatus.STOPPING);

        // 胜利的队伍
        COITeam victoryTeam = Entry.getGame().getVictoryTeam();

        // 待结算奖励明细
        // TODO 根据这个列表进行奖励结算
        List<COIPlayerScore> rewardSettlement = Entry.getGame().getRewardSettlement();


        // 公布玩家结算明细
        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()) {

            List<COIScoreDetail> playerDetail = Entry.getGame().getPlayerDetail(p);

            double total = 0;
            LoggerUtils.sendMessage("------ 奖励结算 ------",p);
            for(COIScoreDetail detail : playerDetail){
                total = total + detail.getScore();
                LoggerUtils.sendMessage(detail.toString(),p);
            }
            LoggerUtils.sendMessage("------ 总计："+total,p);
        }

        // 游戏结算进程
        // 1.倒计时开始，
        // 2.游戏状态标为结束中（COIGameStatus.STOPPING）
        // 3.结算奖励
        // 4.倒计时结束后重置服务器
        new BukkitRunnable() {

            int count = 0;

            @Override
            public void run() {

                count ++;

                // 倒计时的秒数
                int countdown = stoppingTimer - count;

                for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

                    if(countdown > 10){
                        // 公布游戏结果
                        Title title = Title.title(

                                // todo 颜色调整，放入配置文件中配置，倒计时秒数变成配置变量
                                Component.text(LoggerUtils.replaceColor(victoryTeam.getType().getColor()+victoryTeam.getType().getName()+" &f胜利！")),
                                Component.text("奖励已结算，可以在左下角查看"),
                                Title.DEFAULT_TIMES);
                        p.showTitle(title);


                    }else{
                        // 倒计时最后10秒
                        Title title = Title.title(

                                // todo 颜色调整，放入配置文件中配置，倒计时秒数变成配置变量
                                Component.text(LoggerUtils.replaceColor("&c"+countdown+"秒后游戏结束...")),
                                Component.text("奖励已结算，可以在左下角查看"),
                                Title.DEFAULT_TIMES);
                        p.showTitle(title);
                    }


                }

                if(count >= stoppingTimer){
                    // TODO 重置当前服务器
                    this.cancel();
                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 20);


    }
}
