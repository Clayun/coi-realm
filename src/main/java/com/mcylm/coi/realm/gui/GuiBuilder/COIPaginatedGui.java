package com.mcylm.coi.realm.gui.GuiBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.mcylm.coi.realm.gui.SkinGUI;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Slot;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.paginated.PaginatedGui;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class COIPaginatedGui extends Gui {

    private final MenuScheme scheme;
    private final List<Integer> itemSlots;
    private final int nextPageSlot;
    private final int previousPageSlot;
    private final int customSlot;
    private final Function<PageInfo, ItemStack> nextPageItem;
    private final Function<PageInfo, ItemStack> previousPageItem;
    private final Function<PageInfo, ItemStack> customItem;
    private List<Item> content;
    private int page;

    public COIPaginatedGui(Function<COIPaginatedGui, List<Item>> content, Player player, COIPageGuiBuilder model) {
        super(player, model.getLines(), model.getTitle());
        this.content = ImmutableList.copyOf((Collection)content.apply(this));
        this.page = 1;
        this.scheme = model.getScheme();
        this.itemSlots = ImmutableList.copyOf(model.getItemSlots());
        this.nextPageSlot = model.getNextPageSlot();
        this.previousPageSlot = model.getPreviousPageSlot();
        this.customSlot = model.getCustomSlot();
        this.nextPageItem = model.getNextPageItem();
        this.previousPageItem = model.getPreviousPageItem();
        this.customItem = model.getCustomItem();
    }

    public void redraw() {
        this.scheme.apply(this);
        List<Integer> slots = new ArrayList(this.itemSlots);
        List<List<Item>> pages = Lists.partition(this.content, slots.size());
        if (this.page < 1) {
            this.page = 1;
        } else if (this.page > pages.size()) {
            this.page = Math.max(1, pages.size());
        }

        List<Item> page = pages.isEmpty() ? new ArrayList() : (List)pages.get(this.page - 1);
        Slot slot;

        this.setItem(this.previousPageSlot, ItemStackBuilder.of((ItemStack)this.previousPageItem.apply(PageInfo.create(this.page, pages.size()))).build(() -> {
            --this.page;
            this.redraw();
        }));

        this.setItem(this.nextPageSlot, ItemStackBuilder.of((ItemStack)this.nextPageItem.apply(PageInfo.create(this.page, pages.size()))).build(() -> {
            ++this.page;
            this.redraw();
        }));

        this.setItem(this.customSlot,ItemStackBuilder.of((ItemStack)this.customItem.apply(PageInfo.create(this.page, pages.size()))).build(() -> {
           new SkinGUI(getPlayer());
        }));

        if (!this.isFirstDraw()) {
            slots.forEach(this::removeItem);
        }

        Iterator var7 = ((List)page).iterator();

        while(var7.hasNext()) {
            Item item = (Item)var7.next();
            int index = (Integer)slots.remove(0);
            this.setItem(index, item);
        }

    }

    public void updateContent(List<Item> content) {
        this.content = ImmutableList.copyOf(content);
    }
}
