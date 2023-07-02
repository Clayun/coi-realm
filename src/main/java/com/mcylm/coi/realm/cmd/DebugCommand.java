package com.mcylm.coi.realm.cmd;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.gui.ChooseTeamGUI;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.npc.monster.COIPillagerCreator;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

        if(args.length == 0){
            LoggerUtils.sendMessage("参数不对。",commandSender);
            return false;
        }

        if(!commandSender.isOp()){
            LoggerUtils.sendMessage("没有权限。",commandSender);
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

        if(args[0].equalsIgnoreCase("speed")){

            if(args.length < 3){
                LoggerUtils.sendMessage("参数错误。",commandSender);
                return false;
            }
            String targetPlayerName = args[1];

            Player target = Bukkit.getPlayer(targetPlayerName);

            if(target != null){
                // 更改移动速度
                LivingEntity entity = target;
                entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Double.valueOf(args[2]));

            }
        }

        if(args[0].equalsIgnoreCase("fly")){

            if(args.length < 3){
                LoggerUtils.sendMessage("参数错误。",commandSender);
                return false;
            }
            String targetPlayerName = args[1];

            Player target = Bukkit.getPlayer(targetPlayerName);

            if(target != null){
                // 更改飞行速度
                target.setFlySpeed(Float.valueOf(args[2]));
            }
        }

        if(args[0].equalsIgnoreCase("test")){

        }

        return true;
    }
}
