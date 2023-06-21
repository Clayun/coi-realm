package com.mcylm.coi.realm.gui.GuiBuilder;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIUnlockType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.building.FloatableBuild;
import com.mcylm.coi.realm.tools.building.LineBuild;
import com.mcylm.coi.realm.tools.selection.AreaSelector;
import com.mcylm.coi.realm.tools.selection.FloatableSelector;
import com.mcylm.coi.realm.tools.selection.LineSelector;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import lombok.Getter;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.paginated.PaginatedGui;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@NonnullByDefault
public class COIPageGuiBuilder{
    public static final int DEFAULT_LINES = 6;
    public static final int DEFAULT_NEXT_PAGE_SLOT = (Integer)(new MenuScheme()).maskEmpty(5).mask("000000010").getMaskedIndexes().get(0);
    public static final int DEFAULT_PREVIOUS_PAGE_SLOT = (Integer)(new MenuScheme()).maskEmpty(5).mask("010000000").getMaskedIndexes().get(0);
    public static final int DEFAULT_CUSTOM_SLOT = (Integer)(new MenuScheme()).maskEmpty(5).mask("000010000").getMaskedIndexes().get(0);
    public static final List<Integer> DEFAULT_ITEM_SLOTS = (new MenuScheme()).mask("011111110").mask("011111110").mask("011111110").mask("011111110").mask("011111110").getMaskedIndexesImmutable();
    public static final MenuScheme DEFAULT_SCHEME;
    public static final Function<PageInfo, ItemStack> DEFAULT_NEXT_PAGE_ITEM;
    public static final Function<PageInfo, ItemStack> DEFAULT_PREVIOUS_PAGE_ITEM;
    public static final Function<PageInfo, ItemStack> DEFAULT_CUSTOM_ITEM;
    private int lines = 6;
    private String title;
    private List<Integer> itemSlots;
    private int nextPageSlot;
    private int previousPageSlot;
    private int customSlot;
    private MenuScheme scheme;
    private Function<PageInfo, ItemStack> nextPageItem;
    private Function<PageInfo, ItemStack> previousPageItem;

    private Function<PageInfo, ItemStack> customItem;

    public static COIPageGuiBuilder create() {
        return new COIPageGuiBuilder();
    }

    private COIPageGuiBuilder() {
        this.itemSlots = DEFAULT_ITEM_SLOTS;
        this.nextPageSlot = DEFAULT_NEXT_PAGE_SLOT;
        this.previousPageSlot = DEFAULT_PREVIOUS_PAGE_SLOT;
        this.customSlot = DEFAULT_CUSTOM_SLOT;
        this.scheme = DEFAULT_SCHEME;
        this.nextPageItem = DEFAULT_NEXT_PAGE_ITEM;
        this.previousPageItem = DEFAULT_PREVIOUS_PAGE_ITEM;
        this.customItem = DEFAULT_CUSTOM_ITEM;
    }

    public COIPageGuiBuilder copy() {
        COIPageGuiBuilder copy = new COIPageGuiBuilder();
        copy.lines = this.lines;
        copy.title = this.title;
        copy.itemSlots = this.itemSlots;
        copy.nextPageSlot = this.nextPageSlot;
        copy.previousPageSlot = this.previousPageSlot;
        copy.scheme = this.scheme.copy();
        copy.nextPageItem = this.nextPageItem;
        copy.previousPageItem = this.previousPageItem;
        return copy;
    }

    public COIPageGuiBuilder lines(int lines) {
        this.lines = lines;
        return this;
    }

    public COIPageGuiBuilder title(String title) {
        this.title = title;
        return this;
    }

    public COIPageGuiBuilder itemSlots(List<Integer> itemSlots) {
        this.itemSlots = ImmutableList.copyOf(itemSlots);
        return this;
    }

    public COIPageGuiBuilder nextPageSlot(int nextPageSlot) {
        this.nextPageSlot = nextPageSlot;
        return this;
    }

    public COIPageGuiBuilder customSlot(int customSlot) {
        this.customSlot = customSlot;
        return this;
    }

    public COIPageGuiBuilder previousPageSlot(int previousPageSlot) {
        this.previousPageSlot = previousPageSlot;
        return this;
    }

    public COIPageGuiBuilder scheme(MenuScheme scheme) {
        this.scheme = (MenuScheme)Objects.requireNonNull(scheme, "scheme");
        return this;
    }

    public COIPageGuiBuilder nextPageItem(Function<PageInfo, ItemStack> nextPageItem) {
        this.nextPageItem = (Function)Objects.requireNonNull(nextPageItem, "nextPageItem");
        return this;
    }

    public COIPageGuiBuilder previousPageItem(Function<PageInfo, ItemStack> previousPageItem) {
        this.previousPageItem = (Function)Objects.requireNonNull(previousPageItem, "previousPageItem");
        return this;
    }

    public COIPageGuiBuilder customItem(Function<PageInfo, ItemStack> skinItem) {
        this.customItem = (Function)Objects.requireNonNull(skinItem, "skinItem");
        return this;
    }

    public COIPaginatedGui build(Player player, Function<COIPaginatedGui, List<Item>> content) {
            return new COIPaginatedGui(content, player,this);

    }

    static {
        DEFAULT_SCHEME = (new MenuScheme(StandardSchemeMappings.STAINED_GLASS)).mask("100000001").mask("100000001").mask("100000001").mask("100000001").mask("100000001").mask("100000001").scheme(new int[]{3, 3}).scheme(new int[]{3, 3}).scheme(new int[]{3, 3}).scheme(new int[]{3, 3}).scheme(new int[]{3, 3}).scheme(new int[]{3, 3});
        DEFAULT_NEXT_PAGE_ITEM = (pageInfo) -> {
            return ItemStackBuilder.of(Material.ARROW).name("&b&m--&b>").lore("&fSwitch to the next page.").lore("").lore("&7Currently viewing page &b" + pageInfo.getCurrent() + "&7/&b" + pageInfo.getSize()).build();
        };
        DEFAULT_PREVIOUS_PAGE_ITEM = (pageInfo) -> {
            return ItemStackBuilder.of(Material.ARROW).name("&b<&b&m--").lore("&fSwitch to the previous page.").lore("").lore("&7Currently viewing page &b" + pageInfo.getCurrent() + "&7/&b" + pageInfo.getSize()).build();
        };
        DEFAULT_CUSTOM_ITEM = (pageInfo) -> {
            return ItemStackBuilder.of(Material.ARROW).name("&b<&b&m--").lore("&fSwitch to the custom page.").lore("").build();
        };
    }
}

