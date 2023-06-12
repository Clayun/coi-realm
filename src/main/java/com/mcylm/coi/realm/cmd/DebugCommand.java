package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.gui.ChooseTeamGUI;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.SkullUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)){
            // 这个指令只能让玩家使用
            // This command only player can use
            LoggerUtils.sendMessage("这个指令只能让玩家使用。",commandSender);
            return false;
        }
        if (args[0].equalsIgnoreCase("team")) {
            new ChooseTeamGUI(player).open();
        }
        if (args[0].equalsIgnoreCase("monster")) {
            try {
                COIBuilding building = Entry.getInstance().getBuildingManager().getBuildingTemplateByType(COIBuildingType.MONSTER_BASE);
                building.setNpcCreators(List.of(COIPillagerCreator.initCOIPillagerCreator(null)));
                building.setTeam(TeamUtils.getMonsterTeam());

                Location clone = player.getLocation().clone();
                clone.setY(clone.getY() - 1);
                building.build(clone,TeamUtils.getMonsterTeam(),false );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        if(args[0].equalsIgnoreCase("test")){
            ItemStack head = SkullUtils.createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19");

            player.getInventory().addItem(head);
            player.updateInventory();
        }

        return true;
    }
}
