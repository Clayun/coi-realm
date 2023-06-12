package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COIUnlockType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.tools.selection.AreaSelector;
import com.mcylm.coi.realm.tools.selection.LineSelector;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
public class BuilderGUI{

    // 建造的位置
    private Location location;

    public static final List<Integer> ITEM_SLOTS = new MenuScheme()
            .mask("000000000")
            .mask("001111100")
            .mask("011111110")
            .mask("011111110")
            .mask("001111100")
            .mask("000101000")
            .getMaskedIndexesImmutable();

    public static final MenuScheme SCHEME = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111111111")
            .mask("110000011")
            .mask("100000001")
            .mask("100000001")
            .mask("110000011")
            .mask("111010111")
            .scheme(0, 0, 0, 0, 0, 0, 0, 0, 0)
            .scheme(0, 0, 0, 0)
            .scheme(0, 0)
            .scheme(0, 0)
            .scheme(0, 0, 0, 0)
            .scheme(0, 0, 0, 0, 0, 0, 0);

    public BuilderGUI(Player p, Location loc) {

        this.location = loc;

        COITeam team = TeamUtils.getTeamByPlayer(p);

        // 未加入小队的，还有等待中的时候，都打开选队GUI
        if (team == null || Entry.getGame().getStatus().equals(COIGameStatus.WAITING)) {
            LoggerUtils.sendMessage("请选择你要加入的小队", p);
            ChooseTeamGUI chooseTeamGUI = new ChooseTeamGUI(p);
            chooseTeamGUI.open();
            return;
        }

        PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();

        builder.title("&b&l选择你要的建筑");
        builder.previousPageSlot(49);
        builder.nextPageSlot(51);
        builder.nextPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&a下一页").build());
        builder.previousPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&a上一页").build());
        builder.scheme(SCHEME);
        builder.itemSlots(ITEM_SLOTS);

        builder.build(p, paginatedGui -> {
            List<Item> items = new ArrayList<>();
            for (COIBuilding building : Entry.getInstance().getBuildingManager().getAllBuildingTemplates()) {

                if(building.getConfig().isShowInMenu()){

                    ItemStack item = building.getType().getItemType();
                    // 判断是否达到解锁条件
                    if(COIUnlockType.checkUnlock(team,building.getType())){
                        items.add(ItemStackBuilder.of(item.clone())
                                .name(building.getType().getName())
                                .amount(getBuildingNum(team.getBuildingByType(building.getType())))
                                .lore("")
                                .lore("&f> &a可造数量： &c" + team.getBuildingByType(building.getType()).size() +"&7/"+building.getMaxBuild())
                                .lore("&f> &a所需耗材： &c" + building.getConsume())
                                .lore("&f> &a拥有材料： &c" + building.getPlayerHadResource(p))
                                .lore("&f> &a介绍：")
                                .lore(autoLineFeed(building.getType().getIntroduce()))
                                .lore("")
                                .lore("&f> &a&l点击进行建造")
                                .build(() -> {
                                    // 点击时触发下面的方法

                                    if(team.getBuildingByType(building.getType()).size() < building.getMaxBuild()){
                                        // 建造数量没有满的时候，可以建造
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
                                    }else{
                                        paginatedGui.close();
                                        LoggerUtils.sendMessage("&c当前建筑数量已到最大限制！",p);
                                    }

                                }));
                    }else{
                        // 不满足解锁条件
                        COIUnlockType unlockItem = COIUnlockType.getUnlockItem(building.getType());

                        ItemStack itemType = unlockItem.getItemType();

                        if(unlockItem != null){
                            items.add(ItemStackBuilder.of(itemType.clone())
                                    .name(unlockItem.getName())
                                    .amount(1)
                                    .lore("")
                                    .lore("&f> &a解锁条件：")
                                    .lore(autoLineFeed(unlockItem.getIntroduce()))
                                    .lore("")
                                    .lore("&f> &a&l快去解锁吧")
                                    .build(paginatedGui::close));
                        }

                    }

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

        int maxLineLength = 12;
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
