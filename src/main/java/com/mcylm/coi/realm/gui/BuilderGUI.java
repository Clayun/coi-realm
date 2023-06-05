package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.tools.selection.AreaSelector;
import com.mcylm.coi.realm.tools.selection.LineSelector;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 小队建筑中心GUI
 * 在这里选择要建造的建筑，并开始建造
 * Team building GUI
 * Player can choose building here,
 * and pick a place to build.
 */
public class BuilderGUI {

    // 建造的位置
    private Location location;


    public BuilderGUI(Player p, Location loc) {

        this.location = loc;

        COITeam team = TeamUtils.getTeamByPlayer(p);

        if (team == null) {
            LoggerUtils.sendMessage("你还未加入任何小队", p);
            ChooseTeamGUI chooseTeamGUI = new ChooseTeamGUI(p);
            chooseTeamGUI.open();
            return;
        }

        PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();

        builder.title("&b&l选择你要的建筑");
        builder.previousPageSlot(44);
        builder.nextPageSlot(53);
        builder.nextPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&a下一页").build());
        builder.previousPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&a上一页").build());

        builder.build(p, paginatedGui -> {
            List<Item> items = new ArrayList<>();
            for (COIBuilding building : Entry.getInstance().getBuildingManager().getAllBuildingTemplates()) {

                if(building.getType().isInGUI()){
                    items.add(ItemStackBuilder.of(building.getType().getItemType())
                            .name(building.getType().getName())
                            .amount(getBuildingNum(team.getBuildingByType(building.getType())))
                            .lore("")
                            .lore("&f> &a已造数量： &c" + team.getBuildingByType(building.getType()).size())
                            .lore("&f> &a所需耗材： &c" + building.getConsume())
                            .lore("&f> &a拥有材料： &c" + building.getPlayerHadResource(p))
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
                                    if (building instanceof LineBuild lineBuild) {
                                        new LineSelector(p, lineBuild, location);
                                    } else {
                                        new AreaSelector(p, building, location);
                                    }
                                } else {
                                    building.build(location, p);
                                }

                                paginatedGui.close();
                            }));
                }

            }
            return items;
        }).open();




    }

    /**
     * 获取建筑类型建造的数量
     *
     * @return
     */
    private int getBuildingNum(List<COIBuilding> buildings) {

        // 如果没有，就默认1
        if (buildings == null
                || buildings.size() == 0) {
            return 1;
        }
        int maxStackSize = buildings.get(0).getType().getItemType().getMaxStackSize();

        if (buildings.size() > maxStackSize) {
            return maxStackSize;
        }
        return buildings.size();
    }

    /**
     * 介绍自动换行
     *
     * @param introduce
     * @return
     */
    private List<String> autoLineFeed(String introduce) {
        if (introduce == null || introduce.isEmpty()) {
            return Collections.emptyList();
        }

        int maxLineLength = 10;
        List<String> lines = new ArrayList<>();
        int length = introduce.length();
        int count = length / maxLineLength;
        if (length % maxLineLength != 0) {
            count++;
        }

        for (int i = 0; i < count; i++) {
            int start = i * maxLineLength;
            int end = Math.min(start + maxLineLength, length);
            lines.add("  &6" +introduce.substring(start, end));
        }

        return lines;
    }
}
