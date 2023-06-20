package com.mcylm.coi.realm.runnable;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.game.COIGame;
import com.mcylm.coi.realm.model.COIPlayerScore;
import com.mcylm.coi.realm.model.COIScoreDetail;
import com.mcylm.coi.realm.runnable.api.GameTaskApi;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.ServerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
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

                        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

                            Title.Times times = Title.Times.times(Ticks.duration(0L), Ticks.duration(70L), Ticks.duration(0L));

                            Title title = Title.title(
                                    Component.text(LoggerUtils.replaceColor("&c战斗开始！")),
                                    Component.text(LoggerUtils.replaceColor("&f使用背包里的 &c建筑蓝图 &f来建造吧")),
                                    times);
                            p.showTitle(title);
                        }

                        // 倒计时完成
                        // 获取全部没有选择队伍的玩家，按顺序自动匹配进去
                        TeamUtils.autoJoinTeam();

                        // 全部玩家传送到默认出生点
                        TeamUtils.tpAllPlayersToSpawner();

                        // 初始化背包
                        Entry.getGame().initPlayerGaming();

                        // 开始下一个游戏中进程
                        gaming();


                        // 关闭当前task
                        this.cancel();

                    }else{

                        // 倒计时的秒数
                        int countdown = waitingTimer - count;

                        // 展示倒计时信息
                        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

                            Title.Times times = Title.Times.times(Ticks.duration(0L), Ticks.duration(70L), Ticks.duration(0L));

                            Title title = Title.title(
                                    Component.text(LoggerUtils.replaceColor("&f"+countdown+" &c战斗准备中...")),
                                    Component.text(LoggerUtils.replaceColor("&f使用背包里的 &c指南针 &f来选择队伍吧")),
                                    times);
                            p.showTitle(title);
                        }
                    }
                }else{
                    // 不满足最小人数，倒计时重置
                    count = 0;

                    for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

                        Title.Times times = Title.Times.times(Ticks.duration(0L), Ticks.duration(70L), Ticks.duration(0L));

                        Title title = Title.title(
                                Component.text(LoggerUtils.replaceColor("&f"+size+"&7 / "+minPlayers+" &c即将开始！")),
                                Component.text(LoggerUtils.replaceColor("&f使用背包里的 &c指南针 &f来选择队伍吧")),
                                times);
                        p.showTitle(title);
                    }

                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 20);


    }

    @Override
    public void gaming() {
        // 游戏状态标为游戏中
        Entry.getGame().setStatus(COIGameStatus.GAMING);
        // 记录游戏开始时间
        Entry.getGame().setStartTime(LocalDateTime.now());
        // 生成矿脉
        VeinGenerateTask.runTask();
        // 游戏中进程
        // 1.开启倒计时
        // 2.游戏结束后启动 GameStoppingTask
        new BukkitRunnable() {

            BossBar bossBar = BossBar.bossBar(
                    Component.text(LoggerUtils.replaceColor("&c战斗开始了，快速获取战备物资，击败他们")),
                    1,
                    BossBar.Color.RED,
                    BossBar.Overlay.NOTCHED_10);

            Long count = 0l;

            @Override
            public void run() {

                LocalDateTime now = LocalDateTime.now();

                Duration duration = Duration.between(Entry.getGame().getStartTime(),now);

                // 已经过去的秒数
                count = duration.getSeconds();

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
                    Long countdown = gamingTimer - count;
                    // boss bar 的进度条
                    float progress = countdown.floatValue() / gamingTimer.floatValue();

                    if(count >= 3){
                        bossBar.name(Component.text(LoggerUtils.replaceColor("&c战斗还有 &f" + convertSecondsToHHmmss(countdown) + " &c结束！")));
                        bossBar.progress(progress);

                        // 游戏在进行中，倒计时需要在 boss bar 中展示
                        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                            p.showBossBar(bossBar);

                            if(!p.getWorld().getName().equals(Entry.WORLD)){
                                LoggerUtils.sendMessage("&c正在进入游戏", p);
                                TeamUtils.tpSpawner(p);
                            }
                        }
                    }else{
                        bossBar.name(Component.text(LoggerUtils.replaceColor("&c战斗开始了，&6请先快速获取战备物资！")));
                        bossBar.progress(progress);

                        // 游戏在进行中，倒计时需要在 boss bar 中展示
                        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
                            p.showBossBar(bossBar);
                        }
                    }

                    // 检查游戏是否结束
                    if(Entry.getGame().checkGameComplete()){
                        // 下一秒进入结算回合
                        count = Long.valueOf(gamingTimer);
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

            // 生成本局游戏结算记录
            Entry.getGame().initPlayerStopping(p,playerDetail);
        }

        // 游戏结算进程
        // 1.倒计时开始，
        // 2.游戏状态标为结束中（COIGameStatus.STOPPING）
        // 3.结算奖励
        // 4.倒计时结束后重置服务器
        new BukkitRunnable() {

            int count = 0;

            Title.Times times = Title.Times.times(Ticks.duration(0L), Ticks.duration(70L), Ticks.duration(0L));

            @Override
            public void run() {

                count ++;

                // 倒计时的秒数
                int countdown = stoppingTimer - count;

                for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

                    if(countdown > 10){
                        
                        String message;

                        if(victoryTeam != null){
                            message = LoggerUtils.replaceColor(victoryTeam.getType().getColor()+victoryTeam.getType().getName()+" &f胜利！");
                        }else{
                            message = LoggerUtils.replaceColor("&a平局");
                        }

                        // 公布游戏结果
                        Title title = Title.title(
                                Component.text(message),
                                Component.text("奖励已结算，可以在左下角查看"),
                                times);
                        p.showTitle(title);


                    }else{

                        // 提前5秒传送玩家
                        int fakeCountDown = countdown - 5;

                        if(fakeCountDown > 0){
                            // 倒计时最后10秒
                            Title title = Title.title(
                                    Component.text(LoggerUtils.replaceColor("&f"+fakeCountDown+" &c即将结束...")),
                                    Component.text(LoggerUtils.replaceColor("&f奖励已结算，可以在&6左下角&f查看")),
                                    times);
                            p.showTitle(title);
                        }

                    }


                }

                // 倒计时5秒的时候就开始传送
                if(stoppingTimer - count == 5){
                    for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){

                        Title.Times times = Title.Times.times(Ticks.duration(0L), Ticks.duration(70L), Ticks.duration(0L));

                        Title title = Title.title(
                                Component.text(LoggerUtils.replaceColor("&c战斗结束！")),
                                Component.text(LoggerUtils.replaceColor("&f即将传送回主服务器")),
                                times);
                        p.showTitle(title);

                        if(Entry.getInstance().getConfig().getBoolean("bungeecord")){

                            // 大厅服务器名称
                            String lobby = Entry.getInstance().getConfig().getString("bungeecord-lobby");

                            Entry.runSync(new BukkitRunnable() {
                                @Override
                                public void run() {
                                    // 关闭当前服务器
                                    ServerUtils.teleport(p,lobby);
                                }
                            });
                        }


                    }
                }

                if(count >= stoppingTimer){

                    Entry.runSync(new BukkitRunnable() {
                        @Override
                        public void run() {
                            // 关闭当前服务器
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"restart");
                        }
                    });
                    this.cancel();
                }

            }
        }.runTaskTimerAsynchronously(Entry.getInstance(), 20, 20);


    }

    private String convertSecondsToHHmmss(Long totalSeconds) {

        if(totalSeconds  > 3600){
            // 大于一小时
            Duration duration = Duration.ofSeconds(totalSeconds);
            long hours = duration.toHours();
            long minutes = duration.minusHours(hours).toMinutes();
            long remainingSeconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
            return String.format("%02d小时%02d分钟%02d秒", hours, minutes, remainingSeconds);
        }else{
            long minutes = totalSeconds / 60; // 计算分钟数
            long seconds = totalSeconds % 60; // 计算剩余的秒数
            return String.format("%02d分钟%02d秒", minutes, seconds); // 将分钟数和秒数格式化为字符串
        }


    }
}
