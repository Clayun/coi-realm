package com.mcylm.coi.realm.game;

import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏流程控制
 * 游戏从开始到结束的全流程控制类
 */
@Data
public class COIGame {

    // 游戏状态
    private COIGameStatus status;

    // 一场游戏里的全部小队
    private List<COITeam> teams;

    public COIGame() {
        this.teams = new ArrayList<>();
        this.status = COIGameStatus.WAITING;
    }

    /**
     * 打开选择队伍GUI
     * @param player
     */
    public void openTeamChooseGUI(Player player){
        // 默认初始化6个小队，等待倒计时结束会把有人的
        COITeam black = new COITeam(COITeamType.BLACK);
        COITeam red = new COITeam(COITeamType.RED);
        COITeam purple = new COITeam(COITeamType.PURPLE);
        COITeam green = new COITeam(COITeamType.GREEN);
        COITeam yellow = new COITeam(COITeamType.YELLOW);
        COITeam blue = new COITeam(COITeamType.BLUE);



    }


}
