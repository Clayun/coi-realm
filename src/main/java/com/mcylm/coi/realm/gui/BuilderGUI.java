package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.selection.AreaSelector;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.BuildingUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 小队建筑中心GUI
 * 在这里选择要建造的建筑，并开始建造
 * Team building GUI
 * Player can choose building here,
 * and pick a place to build.
 */
public class BuilderGUI extends Gui {

    // 建造的位置
    private Location location;

    // 物品排列方式
    private static final MenuScheme BUTTONS = new MenuScheme()
            .mask("000000000")
            .mask("011111000")
            .mask("000000000")
            ;

    public BuilderGUI(Player player, Location location) {
        super(player, 3, "&b&l选择你要的建筑");
        this.location = location;
    }

    @Override
    public void redraw() {

        // 初始化
        if (isFirstDraw()) {

            COITeam team = TeamUtils.getTeamByPlayer(getPlayer());

            if(team == null){
                LoggerUtils.sendMessage("你还未加入任何小队",getPlayer());
                ChooseTeamGUI chooseTeamGUI = new ChooseTeamGUI(getPlayer());
                chooseTeamGUI.open();
                return;
            }

            // 放置按钮
            MenuPopulator populator = BUTTONS.newPopulator(this);

            for (COIBuilding building : BuildingUtils.getBuildingsTemplate()) {

                populator.accept(ItemStackBuilder.of(building.getType().getItemType())
                        .name(building.getType().getName())
                        .amount(getBuildingNum(team.getBuildingByType(building.getType())))
                        .lore("")
                        .lore("&f> &a已造数量： &c"+team.getBuildingByType(building.getType()).size())
                        .lore("&f> &a所需耗材： &c"+building.getConsume())
                        .lore("&f> &a拥有材料： &c"+building.getPlayerHadResource(getPlayer()))
                        .lore("&f> &a介绍：")
                        .lore(autoLineFeed(building.getType().getIntroduce()))
                        .lore("")
                        .lore("&f> &a&l点击进行建造")
                        .build(() -> {
                            // 点击时触发下面的方法
                            // TODO 封装建造方法

                            building.setTeam(team);
                            // building.build(location,getPlayer());
                            if (building.getStructureByLevel() != null) {
                                new AreaSelector(getPlayer(), building, location);
                            } else {
                                building.build(location, getPlayer());
                            }
                            // 将建筑存入小队
                            team.getFinishedBuildings().add(building);
                            close();
                        }));
            }



        }

    }

    /**
     * 获取建筑类型建造的数量
     * @return
     */
    private int getBuildingNum(List<COIBuilding> buildings){

        // 如果没有，就默认1
        if(buildings == null
            || buildings.size() == 0){
            return 1;
        }else
            return buildings.size();
    }

    /**
     * 介绍自动换行
     * @param introduce
     * @return
     */
    private List<String> autoLineFeed(String introduce){

        if(StringUtils.isBlank(introduce)){
            return new ArrayList<>();
        }

        int length = 10;
        int size = introduce.length() / length;
        if (introduce.length() % length != 0) {
            size += 1;
        }
        return getStrList(introduce, length, size);

    }

    private static List<String> getStrList(String inputString, int length,
                                           int size) {
        List<String> list = new ArrayList<String>();


        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add("  &6"+childStr);
        }
        return list;
    }

    private static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }
}
