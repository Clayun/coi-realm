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
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
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
        } else
            return buildings.size();
    }

    /**
     * 介绍自动换行
     *
     * @param introduce
     * @return
     */
    private List<String> autoLineFeed(String introduce) {

        if (StringUtils.isBlank(introduce)) {
            return new ArrayList<>();
        }

        int length = 10;
        // 修复丢文字BUG
        int size = (int) Math.ceil(introduce.length() / (double)length);
        if (introduce.length() % length != 0) {
            size += 1;
        }
        return getStrList(introduce, length, size);

    }

    private static List<String> getStrList(String inputString, int length,
                                           int size) {
        List<String> list = new ArrayList<>();

//        for (int index = 0; index < size; index++) {
//            String childStr = substring(inputString, index * length,
//                    (index + 1) * length);
//            list.add("  &6" + childStr);
//        }

        for (int i = 0; i < size; i++) {
            if (i == size - 1) { // 处理最后一个子串
                list.add("  &6" + inputString.substring(i * length));
            } else {
                list.add("  &6" + inputString.substring(i * length, (i + 1) * length));
            }
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
