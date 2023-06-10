package com.mcylm.coi.realm.game;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIScoreType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.events.GameStatusEvent;
import com.mcylm.coi.realm.model.COIPlayerScore;
import com.mcylm.coi.realm.model.COIScore;
import com.mcylm.coi.realm.model.COIScoreDetail;
import com.mcylm.coi.realm.player.COIPlayer;
import com.mcylm.coi.realm.runnable.AttackGoalTask;
import com.mcylm.coi.realm.runnable.BasicGameTask;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import com.mcylm.coi.realm.tools.team.impl.COIScoreboard;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏流程控制
 * 游戏从开始到结束的全流程控制类
 */
@Data
public class COIGame {

    // 游戏状态
    // game status
    private COIGameStatus status;

    // 一场游戏里的全部小队
    // all teams in one Game
    private List<COITeam> teams;
    // 玩家缓存
    private Map<Player, COIPlayer> coiPlayers = new HashMap<>();

    public COIGame() {
        this.teams = new ArrayList<>();
        this.status = COIGameStatus.WAITING;

        // 初始化计分板
        new COIScoreboard().showBoard();
        // 初始化小队
        // init team
        setTeams(TeamUtils.initTeams());
        AttackGoalTask.runTask();
    }

    public void setStatus(COIGameStatus status) {
        this.status = status;
        // 触发游戏状态变更事件
        new BukkitRunnable(){
            @Override
            public void run() {
                GameStatusEvent event = new GameStatusEvent(status);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        }.runTask(Entry.getInstance());

    }

    /**
     * 启动游戏
     *
     * 倒计时结束未选择阵营的自动选择一个
     * 每个队都有一个默认的复活点，游戏开始后全体玩家传送到默认复活点
     * 游戏开始后复活点将自动生成一座大本营建筑，大本营被摧毁则小队判定失败，游戏完全结束之后自动结算奖励
     * 玩家可以对大本营进行升级血量，可以建造城墙防御其他玩家，也可以建造自动炮台来防御其他阵营的人或者NPC
     * 可以建造各种设施来制造资源，在敌人的大本营附近建造兵营来发动进攻
     * 也可以自己单枪匹马去偷家
     *
     * 游戏最长60分钟，如果没有分出胜负，则会天降神兵侵略所有小队，最后一个存活的小队获得胜利
     *
     * 游戏结束后会根据玩家的存活时长、综合贡献，以及游戏输赢来结算奖励
     * 结算的硬币可以解锁建筑的皮肤，不同的建筑有不同的皮肤
     *
     */
    public void start(){
        // 启动游戏进程
        new BasicGameTask().waiting();
    }

    /**
     * 获取获得胜利的队伍
     * @return
     */
    public COITeam getVictoryTeam(){

        List<COITeam> stillAliveTeams = new ArrayList<>();


        for(COITeam team : getTeams()){
            if(!team.isDefeat()){
                // 没有被拆除大本营
                stillAliveTeams.add(team);
            }
        }

        // 如果仅剩一个，就它了
        if(stillAliveTeams.size() == 1){
            return stillAliveTeams.get(0);
        }

        COITeam victory = null;
        double score = 0;
        // 如果多个队伍都存货，找一个分数最高的

        for(COITeam team : stillAliveTeams){
            if(team.getScore() > score){
                victory = team;
                score = team.getScore();
            }
        }

        return victory;

    }

    /**
     * 奖励结算
     * 本方法应该在游戏结束后调用
     * 请不要在游戏未结束的时候调用
     * @return 玩家 -> List<COIScore>
     */
    public List<COIPlayerScore> getRewardSettlement(){

        COITeam victoryTeam = getVictoryTeam();

        List<COIPlayerScore> results = new ArrayList<>();

        for(COITeam team : getTeams()){
            List<String> players = team.getPlayers();

            for(String player : players){

                double scoreNumber = 0;
                // 玩家自己的得分
                List<COIScore> playerScore = new ArrayList<>();

                List<COIScore> scoreRecords = team.getScoreRecords();

                for(COIScore score : scoreRecords){
                    if(score.getPlayer().getName().equals(player)){
                        playerScore.add(score);
                        scoreNumber = scoreNumber + score.getType().getScore();
                    }
                }

                if(victoryTeam != null && victoryTeam.equals(team)){
                    // 胜利队伍，全员获奖
                    COIScore victoryBonus = new COIScore(COIScoreType.VICTORY, LocalDateTime.now(),null);
                    scoreNumber = scoreNumber + victoryBonus.getType().getScore();
                    playerScore.add(victoryBonus);
                }

                // 玩家结算数据
                COIPlayerScore coiPlayerScore = new COIPlayerScore();
                coiPlayerScore.setPlayer(player);
                coiPlayerScore.setScore(scoreNumber);
                coiPlayerScore.setScoreList(playerScore);

                // 存值
                results.add(coiPlayerScore);
            }


        }

        return results;
    }

    /**
     * 查询玩家的结算明细
     * @param player
     * @return
     */
    public List<COIScoreDetail> getPlayerDetail(Player player){
        COITeam teamByPlayer = TeamUtils.getTeamByPlayer(player.getName());

        // 临时缓存
        Map<COIScoreType,List<COIScore>> map = new HashMap<>();

        for(COIScore score : teamByPlayer.getScoreRecords()){
            if(map.get(score.getType()) != null){
                map.get(score.getType()).add(score);
            }else{
                List<COIScore> details = new ArrayList<>();
                details.add(score);
                map.put(score.getType(),details);
            }
        }

        List<COIScoreDetail> details = new ArrayList<>();

        for(COIScoreType type : map.keySet()){

            double score = 0;

            List<COIScore> coiScores = map.get(type);

            for(COIScore coiScore : coiScores){
                score = score + coiScore.getType().getScore();
            }

            COIScoreDetail detail = new COIScoreDetail(type,score,coiScores.size());

            details.add(detail);
        }

        return details;
    }

    /**
     * 初始化玩家等待中的背包
     */
    public void initPlayerWaiting(Player p){

        // 先清空玩家背包
        p.getInventory().clear();

        ItemStack itemStack = new ItemStack(Material.COMPASS);
        ItemUtils.rename(itemStack,"&c选择队伍");
        List<String> lore = new ArrayList<>();
        lore.add("请右键打开菜单选择队伍");
        ItemUtils.setLore(itemStack,lore);

        p.getInventory().addItem(itemStack);

        // TODO 可以在这里给于选择建筑皮肤菜单

    }

    /**
     * 初始化玩家游戏中各项设置
     */
    public void initPlayerGaming(){

        for(Player p : Entry.getInstance().getServer().getOnlinePlayers()){
            // 先清空玩家背包
            p.getInventory().clear();

            ItemStack itemStack = new ItemStack(Material.BOOK);
            ItemUtils.rename(itemStack,"&b建筑蓝图");
            List<String> lore = new ArrayList<>();
            lore.add(LoggerUtils.replaceColor("&f游戏必不可少的建筑蓝图"));
            lore.add(LoggerUtils.replaceColor("&c右键&f使用他建造各类建筑"));
            lore.add(LoggerUtils.replaceColor("&f建造需要消耗大量的绿宝石"));
            lore.add(LoggerUtils.replaceColor("&b赶紧带上你的兄弟们挖矿吧"));
            ItemUtils.setLore(itemStack,lore);

            // 初始化建筑蓝图
            p.getInventory().addItem(itemStack);

            // 铁镐头
            ItemStack ironPickaxe = new ItemStack(Material.IRON_PICKAXE);
            p.getInventory().addItem(ironPickaxe);

            // 面包
            ItemStack bread = new ItemStack(Material.BREAD);
            bread.setAmount(32);
            p.getInventory().addItem(bread);

            // 初始化玩家背包默认给的资源
            ItemStack emerald = new ItemStack(Material.EMERALD);

            int num = Entry.getInstance().getConfig().getInt("game.when-start-give-player");

            if(num > 0){
                emerald.setAmount(num);
                p.getInventory().addItem(emerald);
            }

            // 设置玩家的计分板
            p.setScoreboard(Entry.getInstance().getScoreboard());

        }
    }

    /**
     * 游戏结束
     */
    public void initPlayerStopping(Player p,List<COIScoreDetail> playerDetail){

        // 先清空玩家背包
        p.getInventory().clear();

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemUtils.rename(book,"&b本局战绩");

        // 获取书的编辑器
        BookMeta meta = (BookMeta) book.getItemMeta();

        // 设置书的标题和作者
        meta.setTitle("本局战绩");
        meta.setAuthor(p.getName());

        String records = "";

        for(COIScoreDetail detail : playerDetail){
            records = records + detail.toString() + "\n";
        }

        // 设置书的内容
        meta.addPages(Component.text(LoggerUtils.replaceColor(records)));

        // 将编辑器应用到书上
        book.setItemMeta(meta);

        // 将书放入玩家的物品栏中
        p.getInventory().addItem(book);

    }



}
