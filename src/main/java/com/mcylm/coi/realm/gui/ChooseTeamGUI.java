package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.entity.Player;

public class ChooseTeamGUI extends Gui {

    // 物品排列方式
    private static final MenuScheme BUTTONS = new MenuScheme()
            .mask("000000000")
            .mask("001010100")
            .mask("000000000")
            .mask("001010100")
            .mask("000000000");

    public ChooseTeamGUI(Player player) {
        super(player, 5, "&a&l请选择你的小队");
    }

    @Override
    public void redraw() {

        // 放置按钮
        MenuPopulator populator = BUTTONS.newPopulator(this);
        for (COITeam team : Entry.getGame().getTeams()) {
            if (team.getType() == COITeamType.MONSTER) {
                continue;
            }
            populator.accept(ItemStackBuilder.of(team.getType().getBlockColor())
                    .name(team.getType().getColor() + team.getType().getName())
                    .amount(getChooseTeamGUIPeopleAmount(team))
                    .lore("")
                    .lore("&f> &a人数： &c"+team.getPlayers().size() + "/" +Entry.MAX_GROUP_PLAYERS)
                    .lore("&f> &a成员：")
                    .lore(team.getPlayerListName())
                    .lore("")
                    .lore("&f> &7点击加入当前小队")
                    .build(() -> {
                        // 点击时触发下面的方法
                        boolean join = team.join(getPlayer());
                        if(join){
                            LoggerUtils.sendMessage("&7加入 "+team.getType().getColor() + team.getType().getName()+" &7成功",getPlayer());
                        }
                        redraw();
                    }));
        }

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