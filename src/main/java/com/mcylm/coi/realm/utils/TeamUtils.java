package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGUIType;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 队伍控制工具类
 */
public class TeamUtils {

    /**
     * 打开选择队伍GUI并保持数字同步更新
     * @param player
     */
    public static void openTeamChooseGUI(Player player){

        List<COITeam> teams = Entry.getGame().getTeams();

        Inventory chooseTeamGUI = Entry.getInstance().getServer().createInventory(null, 45, COIGUIType.CHOOSE_TEAM_GUI.getComponent());

        // 设置摆放位置
        for(COITeam team : teams){

            ItemUtils itemUtil = new ItemUtils(new ItemStack(team.getType().getBlockColor(),getChooseTeamGUIPeopleAmount(team)));
            itemUtil.rename(team.getType().getName(),team.getType().getTextColor());
            itemUtil.addLore("&a当前人数："+team.getPlayers().size());

            chooseTeamGUI.setItem(team.getType().getSlot(),itemUtil.getItemStack());

        }

        player.openInventory(chooseTeamGUI);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()){
                    cancel();
                }

                InventoryView openInventory = player.getOpenInventory();

                if(openInventory.title().equals(COIGUIType.CHOOSE_TEAM_GUI)){
                    List<COITeam> updateTeams = Entry.getGame().getTeams();
                    for(COITeam team : updateTeams){

                        ItemUtils itemUtil = new ItemUtils(new ItemStack(team.getType().getBlockColor(),getChooseTeamGUIPeopleAmount(team)));
                        itemUtil.rename(team.getType().getName(),team.getType().getTextColor());
                        itemUtil.addLore("&a当前人数："+team.getPlayers().size());
                        openInventory.setItem(team.getType().getSlot(),itemUtil.getItemStack());
                        player.updateInventory();
                    }
                }
            }
        }.runTaskTimerAsynchronously(Entry.getInstance(),0,20);

    }

    /**
     * 初始化队伍
     */
    public static List<COITeam> initTeams(){
        // 默认初始化6个小队，等待倒计时结束会把有人的
        COITeam black = new COITeam(COITeamType.BLACK);
        COITeam red = new COITeam(COITeamType.RED);
        COITeam purple = new COITeam(COITeamType.PURPLE);
        COITeam green = new COITeam(COITeamType.GREEN);
        COITeam yellow = new COITeam(COITeamType.YELLOW);
        COITeam blue = new COITeam(COITeamType.BLUE);

        List<COITeam> results = new ArrayList<>();

        results.add(red);
        results.add(yellow);
        results.add(green);
        results.add(blue);
        results.add(purple);
        results.add(black);

        return results;
    }

    /**
     * 获取选择队伍GUI的人数数量
     * @return
     */
    private static int getChooseTeamGUIPeopleAmount(COITeam team){
        int size = team.getPlayers().size();

        if(size == 0) {
            size = 1;
        }

        return size;
    }

}
