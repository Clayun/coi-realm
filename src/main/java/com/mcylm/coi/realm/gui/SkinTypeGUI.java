package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.enums.types.COIBuildingTypes;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkinTypeGUI extends Gui {

    // 物品排列方式
    private static final MenuScheme BUTTONS = new MenuScheme()
            .mask("111111111")
            .mask("111111111")
            .mask("111111111");

    public SkinTypeGUI(Player player) {
        super(player, 5, "&c&l请选择皮肤分类");
    }

    @Override
    public void redraw() {

        // 放置按钮
        MenuPopulator populator = BUTTONS.newPopulator(this);
        for (COIBuilding building : Entry.getInstance().getBuildingManager().getAllBuildingTemplates()) {

            if(building.getConfig().isShowInMenu()){
                ItemStack item = building.getType().getItemType();

                populator.accept(ItemStackBuilder.of(item.clone())
                        .name(building.getType().getName())
                        .amount(1)
                        .lore("")
                        .lore("&f点击查看该建筑的全部皮肤")
                        .build(() -> {
                            new SkinGUI(getPlayer(),building.getType());
                        }));
            }

        }

    }
}