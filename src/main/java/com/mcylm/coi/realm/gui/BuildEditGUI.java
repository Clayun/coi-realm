package com.mcylm.coi.realm.gui;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
            if(building.getConfig().isShowInMenu()){

                if(building.getBuildPlayerName() != null
                    && building.getBuildPlayerName().equals(getPlayer().getName())){
                    // 建筑是当前玩家建造的，才允许拆除

                    populator.accept(ItemStackBuilder.of(Material.BARRIER)
                            .name("&c拆除")
                            .lore("")
                            .lore("&f> &a返还资源： &c"+ building.getDestroyReturn())
                            .lore("&f> &a建筑等级： &b"+building.getLevel() )
                            .lore("&f> &a当前血量： &f"+ building.getHealth())
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
                }else{
                    // 非建筑所属人点开显示

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    // 获取头颅的 SkullMeta
                    SkullMeta meta = (SkullMeta) head.getItemMeta();

                    // 设置头颅的皮肤为玩家的皮肤
                    OfflinePlayer player = Bukkit.getOfflinePlayer(building.getBuildPlayerName());
                    meta.setOwningPlayer(player);

                    // 将修改后的 SkullMeta 应用到头颅 ItemStack 上
                    head.setItemMeta(meta);

                    populator.accept(ItemStackBuilder.of(head)
                            .name("&a"+building.getBuildPlayerName())
                            .lore("")
                            .lore("&f> &a建筑等级： &b"+building.getLevel() )
                            .lore("&f> &a当前血量： &f"+ building.getHealth())
                            .build(this::close)
                    );

                }

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
