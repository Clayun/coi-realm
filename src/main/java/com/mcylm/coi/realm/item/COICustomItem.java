package com.mcylm.coi.realm.item;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class COICustomItem {
    public static final NamespacedKey COI_CUSTOM_ITEM_NAMESPACEDKEY = Entry.getNamespacedKey("coi_custom_item");

    private final int customModelData;
    private final String namespaceKey;
    private final String name;
    private final Material baseMaterial;
    private final boolean unbreakable;
    private final boolean hideFlags;
    private final boolean newItem;
    private final Class<? extends Listener> eventListener;
    private final Consumer<PlayerInteractEvent> itemUseEvent;
    private final Consumer<EntityDamageByEntityEvent> playerHitEntityEvent;
    private final boolean spacingBeforeLore;
    private final String[] lore;
    private final AttributeInformation[] attributeModifiers;
    private final EnchantmentInformation[] enchantments;
    private final Enchantment[] allowedEnchantments;
    private final Enchantment[] forbiddenEnchantments;

    protected ItemStack itemStack;
    private ShopSettings shopSettings;



    protected COICustomItem(int customModelData, @NonNull String namespaceKey, @NonNull String name, @NonNull Material baseMaterial, boolean unbreakable, boolean hideFlags, boolean newItem, @Nullable Class<? extends Listener> eventListener, @Nullable Consumer<PlayerInteractEvent> itemUseEvent, @Nullable Consumer<EntityDamageByEntityEvent> playerHitEntityEvent, boolean spacingBeforeLore, @Nullable String[] lore, @NonNull Map<Attribute, List<AttributeModifier>> attributeModifiers, @NonNull Map<Enchantment, Integer> enchantments, @NonNull List<Enchantment> allowedEnchantments, @NonNull List<Enchantment> forbiddenEnchantments, ShopSettings shopSettings) {
        this.customModelData = customModelData;
        this.namespaceKey = namespaceKey;
        this.name = name;
        this.baseMaterial = baseMaterial;
        this.unbreakable = unbreakable;
        this.hideFlags = hideFlags;
        this.newItem = newItem;
        this.eventListener = eventListener;
        this.itemUseEvent = itemUseEvent;
        this.playerHitEntityEvent = playerHitEntityEvent;
        this.spacingBeforeLore = spacingBeforeLore;
        this.lore = lore;
        this.shopSettings = shopSettings;

        this.attributeModifiers = new AttributeInformation[attributeModifiers.size()];
        int i = 0;
        for (Map.Entry<Attribute, List<AttributeModifier>> entry : attributeModifiers.entrySet()) {
            this.attributeModifiers[i] = new AttributeInformation(entry.getKey(), entry.getValue().toArray(new AttributeModifier[0]));
            i++;
        }

        this.enchantments = new EnchantmentInformation[enchantments.size()];
        i = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            this.enchantments[i] = new EnchantmentInformation(entry.getKey(), entry.getValue());
            i++;
        }

        this.itemStack = new ItemStack(baseMaterial, 1);

        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setCustomModelData(customModelData);
        itemMeta.setUnbreakable(unbreakable);
        itemMeta.displayName(Component.text(name));
        @NotNull PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(COI_CUSTOM_ITEM_NAMESPACEDKEY, PersistentDataType.STRING, getNamespaceKey());
        if (hideFlags) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        if (lore != null) {
            List<Component> loreList = new ArrayList<>();
            if (spacingBeforeLore) {
                loreList.add(Component.text(""));
            }
            for (String line : lore) {
                loreList.add(Component.text(ChatColor.WHITE + line));
            }
            itemMeta.lore(loreList);
        }

        for (AttributeInformation entry : this.attributeModifiers) {
            for (AttributeModifier modifier : entry.getModifiers()) {
                itemMeta.addAttributeModifier(entry.getAttribute(), modifier);
            }
        }

        for (EnchantmentInformation entry : this.enchantments) {
            itemMeta.addEnchant(entry.getEnchantment(), entry.getLevel(), true);
        }

        this.allowedEnchantments = allowedEnchantments.toArray(new Enchantment[0]);
        this.forbiddenEnchantments = forbiddenEnchantments.toArray(new Enchantment[0]);

        this.itemStack.setItemMeta(itemMeta);
    }

    /**
     * Gets the custom model data of the item.
     * @return the custom model data of the item
     */
    public int getCustomModelData() {
        return this.customModelData;
    }

    /**
     * Gets the namespace key of the item.
     * @return the namespace key of the item
     */
    public @NonNull String getNamespaceKey() {
        return this.namespaceKey;
    }

    /**
     * Gets the namespaced key of the item.
     * @return the namespaced key of the item
     */
    public @NonNull NamespacedKey getNamespacedKey() {
        return new NamespacedKey(Entry.getInstance(), this.namespaceKey);
    }

    /**
     * Gets the name of the item.
     * @return the name of the item
     */
    public @NonNull String getName() {
        return this.name;
    }

    /**
     * Gets the base material of the item.
     * @return the base material of the item
     */
    public @NonNull Material getBaseMaterial() {
        return this.baseMaterial;
    }

    /**
     * Gets if the item is unbreakable.
     * @return if the item is unbreakable
     */
    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    /**
     * Gets if the item hides flags.
     * @return if the item hides flags
     */
    public boolean isHideFlags() {
        return this.hideFlags;
    }

    /**
     * Gets if the item is a new item or if it is an extension of an existing item.
     * @return if the item is a new item
     */
    public boolean isNewItem() {
        return this.newItem;
    }

    /**
     * Gets the event listener of the item.
     * @return the event listener of the item
     */
    public @Nullable Class<? extends Listener> getEventListener() {
        return this.eventListener;
    }

    /**
     * Gets the event that is fired when the item is used.
     * @return the event that is fired when the item is used
     */
    public @Nullable Consumer<PlayerInteractEvent> getItemUseEvent() {
        return this.itemUseEvent;
    }

    /**
     * Gets the event that is called when a player hits an entity with the tool.
     * @return the event that is called when a player hits an entity with the tool
     */
    public @Nullable Consumer<EntityDamageByEntityEvent> getPlayerHitEntityEvent() {
        return playerHitEntityEvent;
    }

    /**
     * Gets the item's lore.
     * @return the item's lore
     */
    public @Nullable String[] getLore() {
        return this.lore;
    }

    /**
     * Gets the item's attribute modifiers.
     * @return the item's attribute modifiers
     */
    public @NonNull AttributeInformation[] getAttributeModifiers() {
        return this.attributeModifiers;
    }

    /**
     * Gets the item's enchantments.
     * @return the item's enchantments
     */
    public @NonNull EnchantmentInformation[] getEnchantments() {
        return this.enchantments;
    }

    /**
     * Gets a list of enchantments that can be applied to the item, this can be used if the base material doesn't allow
     * the enchantment by default.
     * @return a list of enchantments that can be applied to the item
     */
    public @Nullable Enchantment[] getAllowedEnchantments() {
        return this.allowedEnchantments;
    }

    /**
     * Gets a list of enchantments that can't be applied to the item, this can be used if the base material allows
     * the enchantment by default.
     * @return a list of enchantments that can't be applied to the item
     */
    public @Nullable Enchantment[] getForbiddenEnchantments() {
        return this.forbiddenEnchantments;
    }

    public @NonNull TriState canEnchant(@NonNull Enchantment enchantment) {
        for (Enchantment allowedEnchantment : this.allowedEnchantments) {
            if (allowedEnchantment.equals(enchantment)) {
                return TriState.TRUE;
            }
        }
        for (Enchantment forbiddenEnchantment : this.forbiddenEnchantments) {
            if (forbiddenEnchantment.equals(enchantment)) {
                return TriState.FALSE;
            }
        }
        return TriState.NOT_SET;
    }

    /**
     * Gets the item stack of the item.
     * @return the item stack of the item
     */
    public @NonNull ItemStack getItemStack() {
        return this.itemStack.clone();
    }

    public ShopSettings shopSettings() {
        return shopSettings;
    }

    /**
     * This class represents the information about an attribute modifier.
     */
    public static class AttributeInformation {
        private final Attribute attribute;
        private final AttributeModifier[] modifier;

        /**
         * Creates a new attribute information.
         * @param attribute the attribute
         * @param modifier the modifier
         */
        public AttributeInformation(@NonNull Attribute attribute, @NonNull AttributeModifier[] modifier) {
            this.attribute = attribute;
            this.modifier = modifier;
        }

        /**
         * Gets the attribute.
         * @return the attribute
         */
        public @NonNull Attribute getAttribute() {
            return this.attribute;
        }

        /**
         * Gets the modifier.
         * @return the modifier
         */
        public @NonNull AttributeModifier[] getModifiers() {
            return this.modifier;
        }
    }

    /**
     * This class represents the information about an enchantment.
     */
    public static class EnchantmentInformation {
        private final Enchantment enchantment;
        private final int level;
        public EnchantmentInformation(@NonNull Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public @NonNull Enchantment getEnchantment() {
            return this.enchantment;
        }

        public int getLevel() {
            return this.level;
        }
    }

    public static class Builder {
        protected final int customModelData;
        protected final String namespaceKey;
        protected final String name;
        protected final Material baseMaterial;

        protected boolean unbreakable = false;
        protected boolean hideFlags = false;
        protected boolean newItem = true;
        protected Consumer<PlayerInteractEvent> itemUseEvent = null;
        protected Consumer<EntityDamageByEntityEvent> playerHitEntityEvent = null;
        protected Class<? extends Listener> eventListener = null;
        protected boolean spacingBeforeLore = true;
        protected String[] lore = null;
        protected Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
        protected Map<Enchantment, Integer> enchantments = new HashMap<>();
        protected List<Enchantment> allowedEnchantments = new ArrayList<>();
        protected List<Enchantment> forbiddenEnchantments = new ArrayList<>();

        private ShopSettings shopSettings;
        /**
         * Creates a new item builder.
         * @param namespaceKey the namespace key of the item
         * @param name the name of the item
         * @param baseMaterial the base material of the item
         */
        public Builder(String namespaceKey, String name, Material baseMaterial) {
            this.customModelData = 0;
            this.namespaceKey = namespaceKey;
            this.name = name;
            this.baseMaterial = baseMaterial;
            this.shopSettings = new ShopSettings().showInShop(false);
        }

        /**
         * Sets if the item is unbreakable.
         * @param unbreakable if the item is unbreakable
         * @return the builder
         */
        public @NonNull Builder unbreakable(boolean unbreakable) {
            this.unbreakable = unbreakable;
            return this;
        }

        /**
         * Sets if the item hides flags.
         * @param hideFlags if the item hides flags
         * @return the builder
         */
        public @NonNull Builder hideFlags(boolean hideFlags) {
            this.hideFlags = hideFlags;
            return this;
        }

        /**
         * Sets if the item is a new item or if it is an extension of an existing item.
         * @param newItem if the item is a new item
         * @return the builder
         */
        public @NonNull Builder newItem(boolean newItem) {
            this.newItem = newItem;
            return this;
        }

        /**
         * Sets the event listener of the item.
         * @param itemUseEvent the event listener of the item
         * @return the builder
         */
        public @NonNull Builder itemUseEvent(@Nullable Consumer<PlayerInteractEvent> itemUseEvent) {
            this.itemUseEvent = itemUseEvent;
            return this;
        }

        /**
         * Sets the event listener of the item. The classes specified here must have a constructor like this:
         * <pre>
         *     public MyListener(CustomItem customItem) {
         *     }
         * </pre>
         *
         * The use of this method is completely optional, you can just register the listener yourself with the usual API.
         *
         * @param eventListener the event listener of the item
         * @return the builder
         */
        public @NonNull Builder eventListener(@Nullable Class<? extends Listener> eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        /**
         * Sets the item lore.
         * @param spacingBeforeLore if there should be spacing before the lore
         * @param lore the lore
         * @return the builder
         */
        public @NonNull Builder lore(boolean spacingBeforeLore, String... lore) {
            this.spacingBeforeLore = spacingBeforeLore;
            this.lore = lore;
            return this;
        }

        /**
         * Sets the item lore.
         * @param lore the lore
         * @return the builder
         */
        public @NonNull Builder lore(@Nullable String... lore) {
            this.spacingBeforeLore = true;
            this.lore = lore;
            return this;
        }

        /**
         * Sets the item lore.
         * @param lore the lore
         * @return the builder
         */
        public @NonNull Builder lore(@Nullable List<String> lore) {
            this.spacingBeforeLore = true;
            if (lore != null) {
                this.lore = lore.toArray(new String[0]);
            } else {
                this.lore = null;
            }
            return this;
        }

        /**
         * Adds an attribute modifier to the item.
         * @param attribute the attribute
         * @param attributeModifier the attribute modifier
         * @return the builder
         */
        public @NonNull Builder attributeModifier(@NonNull Attribute attribute, @NonNull AttributeModifier attributeModifier) {
            this.attributeModifiers.computeIfAbsent(attribute, k -> new ArrayList<>()).add(attributeModifier);
            return this;
        }

        /**
         * Adds an enchantment to the item.
         * @param enchantment the enchantment
         * @param level the level
         * @return the builder
         */
        public @NonNull Builder enchantment(@NonNull Enchantment enchantment, int level) {
            if (this.enchantments.containsKey(enchantment)) {
                throw new IllegalArgumentException("Enchantment " + enchantment.getKey() + " is already added to this item");
            }

            this.enchantments.put(enchantment, level);
            return this;
        }

        /**
         * Adds an allowed enchantment to the item, this can be used if the base material doesn't allow the enchantment
         * by default.
         * @param enchantments the enchantments
         * @return the builder
         */
        public @NonNull Builder allowedEnchantment(@NonNull Enchantment... enchantments) {
            for (Enchantment enchantment : enchantments) {
                if (this.forbiddenEnchantments.contains(enchantment)) {
                    continue;
                }

                this.allowedEnchantments.add(enchantment);
            }
            return this;
        }

        /**
         * Adds a forbidden enchantment to the item, this can be used if the base material allows the enchantment
         * by default.
         * @param enchantments the enchantments
         * @return the builder
         */
        public @NonNull Builder forbiddenEnchantment(@NonNull Enchantment... enchantments) {
            for (Enchantment enchantment : enchantments) {
                if (this.allowedEnchantments.contains(enchantment)) {
                    continue;
                }

                this.forbiddenEnchantments.add(enchantment);
            }
            return this;
        }

        /**
         * Sets the event that is called when a player hits an entity with the tool.
         * @param playerHitEntityEvent the event that is called when a player hits an entity with the tool
         * @return the builder
         */
        public @NonNull Builder playerHitEntityEvent(@Nullable Consumer<EntityDamageByEntityEvent> playerHitEntityEvent) {
            this.playerHitEntityEvent = playerHitEntityEvent;
            return this;
        }

        public @NonNull Builder shopSettings(ShopSettings shopSettings) {
            this.shopSettings = shopSettings;
            return this;
        }

        /**
         * Builds the item.
         * @return the item
         */
        public @NonNull COICustomItem build() {
            return new COICustomItem(this.customModelData, this.namespaceKey, this.name, this.baseMaterial, this.unbreakable, this.hideFlags, this.newItem, this.eventListener, this.itemUseEvent, this.playerHitEntityEvent, this.spacingBeforeLore, this.lore, this.attributeModifiers, this.enchantments, this.allowedEnchantments, this.forbiddenEnchantments, this.shopSettings);
        }
    }


    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class ShopSettings {

        boolean showInShop;
        // 数量
        private int num;
        // 价格
        private int price;

        // 解锁相关的设置
        // 建筑类型
        private COIBuildingType buildingType;
        // 建筑数量
        private int buildingsNum;
        // 前置建筑满足的等级
        private int buildingLevel;

        public static boolean checkUnlock(COITeam team, COICustomItem item) {

            // 是否开启解锁功能
            boolean openLock = Entry.getInstance().getConfig().getBoolean("game.building.lock");

            if (!openLock) {
                return true;
            }


            ShopSettings unlockType = item.shopSettings;
            // 匹配到了
            // 1.检查前置建筑类型是否为null
            if (unlockType.buildingType() == null) {
                // 直接解锁
                return true;
            }

            // 没有限制，直接解锁
            if (unlockType.buildingsNum() == 0
                    && unlockType.buildingLevel() == 0) {
                return true;
            }

            // 2.判断当前小队是否已经解锁前置建筑
            // 前置建筑建造的数量
            int perBuildingCount = 0;
            int maxLevel = 0;
            List<COIBuilding> finishedBuildings = team.getFinishedBuildings();

            for (COIBuilding building : finishedBuildings) {
                // 如果匹配到前置建筑
                if (building.getType().equals(unlockType.buildingType())) {
                    perBuildingCount++;
                    if (building.getLevel() > maxLevel) {
                        maxLevel = building.getLevel();
                    }
                }
            }

            // 如果两个条件同时满足
            if (perBuildingCount >= unlockType.buildingsNum()
                    && maxLevel >= unlockType.buildingLevel()) {
                return true;
            }

            // 未在上面满足条件，未解锁
            return false;
        }

    }

}