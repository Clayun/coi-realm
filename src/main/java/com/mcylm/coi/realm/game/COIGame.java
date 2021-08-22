package com.mcylm.coi.realm.game;

import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COITeamType;
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
        // 初始化小队
        setTeams(TeamUtils.initTeams());
    }






}
