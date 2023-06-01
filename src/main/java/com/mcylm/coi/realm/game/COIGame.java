package com.mcylm.coi.realm.game;

import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.player.COIPlayer;
import com.mcylm.coi.realm.runnable.AttackGoalTask;
import com.mcylm.coi.realm.runnable.BasicGameTask;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    public COIGame() {
        this.teams = new ArrayList<>();
        this.status = COIGameStatus.WAITING;
        // 初始化小队
        // init team
        setTeams(TeamUtils.initTeams());
        AttackGoalTask.runTask();
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

    private Map<Player, COIPlayer> coiPlayers = new HashMap<>();




}
