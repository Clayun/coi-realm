package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.item.COICustomItem;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.InventoryUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class ForgeGUI {

    /**
     * 创建铁匠铺GUI
     * @param p 需要打开GUI的玩家
     * @param building 铁匠铺
     */
    public ForgeGUI(Player p,COIBuilding building) {

        COITeam team = TeamUtils.getTeamByPlayer(p);

        // 未加入小队的，还有等待中的时候，都打开选队GUI
        if (team == null || Entry.getGame().getStatus().equals(COIGameStatus.WAITING)) {
            LoggerUtils.sendMessage("请选择你要加入的小队", p);
            ChooseTeamGUI chooseTeamGUI = new ChooseTeamGUI(p);
            chooseTeamGUI.open();
            return;
        }

        PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();

        builder.title("&9&l选择你要打造的装备");
        builder.previousPageSlot(49);
        builder.nextPageSlot(51);
        builder.nextPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&a下一页").build());
        builder.previousPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&a上一页").build());

        builder.build(p, paginatedGui -> {
            List<Item> items = new ArrayList<>();

            for (COICustomItem customItem : Entry.getInstance().getCustomItemManager().getCustomItems()) {

                // 物品模型
                ItemStack item = customItem.getItemStack();

                List<String> lores = List.copyOf(item.getLore());

                if (!customItem.shopSettings().showInShop()) {
                    continue;
                }

                // 判断是否达到解锁条件
                if(COICustomItem.ShopSettings.checkUnlock(team, customItem)){

                    items.add(ItemStackBuilder.of(item.clone())
                            .name(customItem.getName())
                            .amount(1)
                            .lore("")
                            .lore("&f> &a单价/数量： &c" + customItem.shopSettings().price()+"&7/"+ customItem.shopSettings().num() +"个")
                            .lore("&f> &a背包携带： &c" + building.getPlayerHadResource(p))
                            .lore("&f> &a介绍：")
                            .lore(lores)
                            .lore("")
                            .lore("&f> &a&l点击进行打造")
                            .build(() -> {
                                // 点击时触发下面的方法

                                // 扣除资源，并交付道具
                                boolean b = InventoryUtils.deductionResources(p, customItem.shopSettings().price());

                                if(b){
                                    // 扣除成功
                                    // 交付物品
                                    ItemStack propItem = customItem.getItemStack();

                                    propItem.setAmount(customItem.shopSettings().num());
                                    p.getInventory().addItem(propItem);
                                    LoggerUtils.sendMessage("物品打造完成！", p);

                                }else{
                                    LoggerUtils.sendMessage("&c背包内的资源不够！", p);
                                    paginatedGui.close();
                                }

                            }));
                }else{
                    // 不满足解锁条件

                    ItemStack itemType = new ItemStack(Material.CHEST);

                    items.add(ItemStackBuilder.of(itemType.clone())
                            .name(customItem.getName()+" &c尚未解锁")
                            .amount(1)
                            .lore("")
                            .lore("&f> &a解锁条件：")
                            .lore(GUIUtils.autoLineFeed("建筑等级达到："+ customItem.shopSettings().buildingLevel()))
                            .lore("")
                            .lore("&f> &a&l快去升级吧")
                            .build(paginatedGui::close));

                }
            }
            return items;
        }).open();




    }

}
