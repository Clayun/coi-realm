package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.gui.ChooseTeamGUI;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player)){
            // 这个指令只能让玩家使用
            // This command only player can use
            LoggerUtils.sendMessage("这个指令只能让玩家使用。",commandSender);
            return false;
        }
        Player player = ((Player) commandSender);
        if (args[0].equalsIgnoreCase("team")) {
            new ChooseTeamGUI(player).open();
        }
        if (args[0].equalsIgnoreCase("monster")) {
            try {
                COIBuilding building = Entry.getInstance().getBuildingManager().getBuildingTemplateByType(COIBuildingType.MONSTER_BASE);
                building.setNpcCreators(List.of(COIPillagerCreator.initCOIPillagerCreator(null)));
                building.setTeam(TeamUtils.getMonsterTeam());
                building.build(player.getLocation(),TeamUtils.getMonsterTeam(),false );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        return true;
    }
}
