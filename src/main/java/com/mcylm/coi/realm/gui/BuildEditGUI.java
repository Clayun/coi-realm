package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BuildEditGUI extends Gui {

    private static final MenuScheme BUTTONS = new MenuScheme()
            .mask("000000000")
            .mask("001010100")
            .mask("000000000");

    private final COIBuilding building;

    public BuildEditGUI(Player player, COIBuilding building) {
        super(player, 3, building.getTeam().getType().getColor() + building.getType().getName());
        this.building = building;
    }

    @Override
    public void redraw() {

        if (isFirstDraw()) {


            // 放置按钮
            MenuPopulator populator = BUTTONS.newPopulator(this);

            // 显示在GUI的才算
            if(building.getType().isInGUI()){
                populator.accept(ItemStackBuilder.of(Material.BARRIER)
                        .name("&c拆除")
                        .lore("")
                        .lore("&f> &a返还资源： &c"+ building.getDestroyReturn())
                        .build(() -> {
                            if (!building.isComplete()) {
                                LoggerUtils.sendMessage("&c建筑仍在建造中", getPlayer());
                                return;
                            }
                            if (!building.isAlive()) {
                                LoggerUtils.sendMessage("&c建筑已被拆毁", getPlayer());
                                return;
                            }
                            building.destroy(true);
                            int returnResource = building.getDestroyReturn();
                            int group = returnResource / 64;
                            int amount = returnResource % 64;
                            Material material = building.getResourceType();
                            for (int i = 0; i < group; i++) {
                                getPlayer().getWorld().dropItemNaturally(getPlayer().getLocation(), new ItemStack(material, 64));
                            }

                            getPlayer().getWorld().dropItemNaturally(getPlayer().getLocation(), new ItemStack(material, amount));
                            close();
                        })

                );
            }


            populator.accept(building.getLevel() < building.getMaxLevel() ? ItemStackBuilder.of(Material.BEACON)
                    .name("&a升级建筑")
                    .lore("")
                    .lore("&f> &e当前等级： &c"+building.getLevel())
                    .lore("&f> &e最大等级： &c"+building.getMaxLevel())
                    .lore("&f> &a所需耗材： &c"+building.getUpgradeRequiredConsume())
                    .lore("&f> &a拥有材料： &c"+building.getPlayerHadResource(getPlayer()))
                    .lore("&f> &a&l点击进行升级")
                    .amount(building.getLevel()) // 我相信不过超过64
                    .build(() -> {
                        // 点击时触发下面的方法
                        // TODO 封装建造方法
                        if (!building.isComplete()) {
                            LoggerUtils.sendMessage("&c建筑仍在建造中", getPlayer());
                            return;
                        }
                        if (!building.isAlive()) {
                            LoggerUtils.sendMessage("&c建筑已被拆毁", getPlayer());
                            return;
                        }
                        building.upgrade(getPlayer());

                        close();
                    }) : ItemStackBuilder.of(Material.RED_WOOL)
                    .name("&c建筑已满级")
                    .build(() -> {}));



        }
    }
}
