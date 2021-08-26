package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.FormationUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.Events;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.SimpleSlot;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.SchemeMapping;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class FormationGUI extends Gui {

    public FormationGUI(Player player) {
        super(player, FormationUtils.LINES, "&6选择你的布阵");

        Events.subscribe(InventoryClickEvent.class).filter((e) -> {
            return e.getInventory().getHolder() != null;
        }).filter((e) -> {
            return e.getInventory().getHolder().equals(getPlayer());
        }).handler((e) -> {
            e.setCancelled(false);
        }).bindWith(this);
    }

    @Override
    public void redraw() {

        // 获取最新的Team数据
        COITeam team = TeamUtils.getTeamByPlayer(getPlayer());

        int size = team.getArmyRunner().getNpcList().size();

        if(size == 0){
            return;
        }



        if(team.getBattleFormation().isEmpty()){
            // 如果是空的，就默认生成一个
        }

        MenuScheme me = translate(team.getBattleFormation());

        MenuPopulator populator = me.newPopulator(this);

//        populator.
    }

    /**
     * 生成一个默认的军队阵型
     * @param army 军人数量
     * @return
     */
    private MenuScheme generateArmyFormation(int army){

        // 把玩家也算在里面
        army = army + 1;

        return null;
    }


    /**
     * 将阵型翻译成 MenuScheme
     * @param battleFormation
     * @return
     */
    private MenuScheme translate(List<List<Integer>> battleFormation){

        List<String> mask = new ArrayList<>();
        for(List<Integer> line : battleFormation){
            // 每行
            String lineStr = "";
            for(Integer slot : line){
                // 单元格
                if(slot == 2){
                    // 玩家所在位置，改为1
                    slot = 1;
                }
                lineStr = lineStr + slot;
            }

            mask.add(lineStr);
        }

        String[] objects = (String[]) mask.toArray();
        MenuScheme menuScheme = new MenuScheme().masks(objects);

        return menuScheme;
    }

}
