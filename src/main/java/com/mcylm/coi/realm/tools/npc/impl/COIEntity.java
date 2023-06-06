package com.mcylm.coi.realm.tools.npc.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.runnable.NpcAITask;
import com.mcylm.coi.realm.tools.data.EntityData;
import com.mcylm.coi.realm.tools.npc.AI;
import com.mcylm.coi.realm.utils.GUIUtils;
import com.mcylm.coi.realm.utils.InventoryUtils;
import com.mcylm.coi.realm.utils.ItemUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 所有AI的父类
 * 通用AI，拥有基本生存能力，能捡东西，吃东西
 */
public class COIEntity implements AI {

    // CitizensNPC 实例
    protected NPC npc;
    // COIAI 实例
    private COINpc coiNpc;
    // NPC饱食度
    private double hunger;
    // 饥饿度计数
    private int hungerTick = 0;
    // NPC是否已生成
    private boolean isSpawn = false;
    // Citizens 实例是否已创建
    private boolean isCreated = false;
    // 是否已经非常饥饿，无法工作
    private boolean isHungry = false;
    // 是否彻底清除
    private boolean isRemoved = false;
    // 默认饥饿度
    private static final int DEFAULT_HUNGER = 20;
    // 饥饿度计算值间隔
    private static final int HUNGER_DELAY = 20;
    // 饥饿度计数值
    private int HUNGER_DELAY_COUNT = 0;
    // 查询最近的食物箱子，最大距离
    private int FOOD_CHEST_MAX_DISTANCE = 20;
    // 因为饥饿，每秒掉血数量
    private final double HUNGER_DAMAGE = 0.5d;
    // 每次减少的饱食度
    private final double HUNGER_COST = 0.5d;
    // 最大生命值
    private int MAX_HEALTH = 20;
    // 重生计数值
    private int RESPAWN_DELAY_COUNT = 0;
    // 是否正在重生COUNT
    private boolean isRespawning = false;
    // 最后一次的位置
    private Location lastLocation = null;
    // 想捡的物品
    private Item targetItem;
    // 暂时屏蔽的物品
    private final Cache<Item, Item> ignoredItems = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .build();

    // 悬浮字相关
    private Map<Player, Hologram> holograms = new HashMap<>();
    private Map<Player, AtomicInteger> hologramVisitors = new HashMap<>();

    // 构造NPC
    public COIEntity(COINpc npcCreator) {
        npcCreator.setNpc(this);
        create(npcCreator);

    }

    /**
     * 创建 AI NPC 实例，万物之始
     *
     * @param npcCreator
     * @return
     */
    @Override
    public COIEntity create(COINpc npcCreator) {

        if (npcCreator == null) {
            LoggerUtils.log("npc创建对象为空，无法生成NPC");
            return null;
        }

        this.coiNpc = npcCreator;

        // 创建 CitizensNPC 实例
        this.npc = CitizensAPI.getNPCRegistry().createNPC(npcCreator.getNpcType(), getName());
        this.isCreated = true;
        this.coiNpc.setId(this.npc.getUniqueId().toString());
        initNpcAttributes(npcCreator);

        return this;
    }


    /**
     * 更新 NPC属性
     *
     * @param coiNpc
     * @param respawn 是否重新生成，重新生成会清空背包
     * @return
     */
    @Override
    public COIEntity update(COINpc coiNpc, boolean respawn) {

        if (this.coiNpc == null || !isCreated) {
            LoggerUtils.log("npc从未创建，无法更新");
            return null;
        }

        // 临时缓存老的NPC数据
        COINpc oldNpc = this.coiNpc;

        // 给NPC赋值新的数据
        this.coiNpc = coiNpc;

        if (respawn) {
            // 清空背包
            this.coiNpc.setInventory(GUIUtils.createNpcInventory(3));

            // 如果NPC还存活
            if (isAlive()) {
                // NPC的当前位置
                Location entityLocation = this.npc.getEntity().getLocation();
                // NPC重新生成
                despawn();
                spawn(entityLocation);
            } else if (!isRemoved) {
                // 重生
                despawn();
                setHunger(DEFAULT_HUNGER);
                spawn(coiNpc.getSpawnLocation());
            }

        } else {
            // 还原背包
            this.coiNpc.setInventory(oldNpc.getInventory());
        }


        initNpcAttributes(this.coiNpc);

        return this;
    }

    /**
     * 自动寻路，默认的算法
     *
     * @param location
     */
    @Override
    public void findPath(Location location) {
        if (!isAlive()) {
            return;
        }

        if (!npc.isSpawned()) {
            return;
        }

        if (canStand(location)) {
            npc.faceLocation(location);
            Navigator navigator = npc.getNavigator();
            navigator.getDefaultParameters()
                    .stuckAction(null)
                    .useNewPathfinder(false);

            navigator.setTarget(location);


        }

    }



    /**
     * 方块上方是否可以站立
     *
     * @param location
     * @return
     */
    public boolean canStand(Location location) {
        Location clone1 = location.clone();
        Location clone2 = location.clone();

        clone1.setY(clone1.getY() + 1);
        clone2.setY(clone2.getY() + 2);

        if (clone1.getBlock().getType().equals(Material.AIR)
                && clone2.getBlock().getType().equals(Material.AIR)) {
            return true;
        }

        return false;
    }

    /**
     * 控制 NPC 说话
     *
     * @param message
     */
    @Override
    public void say(String message) {

        if (!isAlive()) {
            return;
        }

        // 最大显示时长（秒）
        int maxShowTimer = 3;

        // 在脑袋顶上显示文字

        // NPC附近20格范围内的玩家都能看到
        List<Entity> nearbyEntities = getNpc().getEntity().getNearbyEntities(20, 20, 20);

        for(Entity entity : nearbyEntities){
            if (entity != null && entity.getType().equals(EntityType.PLAYER)) {
                Player p = (Player) entity;

                LoggerUtils.debug("检测到NPC附近玩家："+p.getName());

                if(p.isOnline()){
                    if (hologramVisitors.containsKey(p)) {
                        hologramVisitors.get(p).set(maxShowTimer);
                    } else {
                        hologramVisitors.put(p, new AtomicInteger(maxShowTimer));
                    }
                    if (!holograms.containsKey(p)) {

                        LoggerUtils.debug("开始展示NPC说的话");

                        // 在NPC头上2.5格的位置显示
                        double floatHeight = 2.8;
                        Location location = getNpc().getEntity().getLocation().clone();
                        location.setY(location.getY() + floatHeight);

                        Hologram hologram = HolographicDisplaysAPI.get(Entry.getInstance()).createHologram(location);
                        hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
                        hologram.getVisibilitySettings().setIndividualVisibility(p, VisibilitySettings.Visibility.VISIBLE);
                        hologram.getLines().appendText(LoggerUtils.replaceColor(message));

                        holograms.put(p, hologram);
                        new BukkitRunnable() {
                            int tick = 0;

                            @Override
                            public void run() {

                                if (!holograms.containsKey(p)) {
                                    Entry.runSync(hologram::delete);
                                    this.cancel();
                                } else {
                                    if (tick++ == 20) {
                                        tick = 0;
                                        if (hologramVisitors.get(p).decrementAndGet() == 0) {
                                            holograms.remove(p);
                                            hologramVisitors.remove(p);
                                        }

                                    }

                                    if(isAlive()){
                                        // 活着的情况下，再去显示
                                        Location newLoc = getNpc().getEntity().getLocation().clone();
                                        newLoc.setY(newLoc.getY() + floatHeight);

                                        Entry.runSync(() -> {
                                            if (hologram.isDeleted()) return;
                                            hologram.setPosition(newLoc);
                                        });
                                    }

                                }
                            }
                        }.runTaskTimerAsynchronously(Entry.getInstance(), 1, 1);

                    }
                }

            }
        }




    }

    /**
     * 吃背包里的食物，恢复饥饿度，从而让血量恢复
     */
    @Override
    public void eatFood() {

        if (!isAlive()) {
            return;
        }


        LivingEntity entity = (LivingEntity) npc.getEntity();

        if (getHunger() >= 15) {
            // 如果饱食度大于等于15，就不再是饥饿状态
            isHungry = false;
        }

        if (getHunger() >= 20) {
            return;
        }

        Inventory backpack = getCoiNpc().getInventory();
        // 在背包里找吃的
        if(!backpack.isEmpty()){
//            LoggerUtils.debug("试图吃东西"); // 太频繁了，取消显示
            Iterator<ItemStack> iterator = backpack.iterator();
            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                if (item != null) {
                    List<String> npcFoods = Entry.getNpcFoods();
                    for (String foodName : npcFoods) {
                        Material material = Material.getMaterial(foodName);

                        if (material != null) {
                            if (item.getType() == material) {
                                if (item.getAmount() >= 1) {
                                    item.setAmount(item.getAmount() - 1);
                                    ItemStack cache = null;

                                    // 如果手中不为空气
                                    if (!entity.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
                                        cache = entity.getEquipment().getItemInMainHand();
                                    }
                                    entity.getEquipment().setItemInMainHand(item);
                                    setHunger(getHunger() + 1);
                                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EAT, 100, 2);
                                    entity.getEquipment().setItemInMainHand(cache);
                                    return;
                                } else {
                                    item.setType(Material.AIR);
                                }
                            }
                        }
                    }


                }

            }

        } else {
            // 如果食物背包里是空的，同时饱食度低于10，就去磨坊拿吃的

            if (getHunger() <= 10) {
                isHungry = true;
            }


        }

    }

    /**
     * 去磨坊寻找食物
     */
    private void findFood() {

        if (!isAlive()) {
            return;
        }

        if (!isTooHungryToWork()) {
            return;
        }

        List<Location> foodChests = getCoiNpc().getTeam().getFoodChests();

        if (foodChests == null
                || foodChests.isEmpty()) {
            LoggerUtils.debug("食物箱子不存在");
            // 食物箱子不存在，就直接原地摆烂
            say("肚子好饿！附近都没有吃的了");
            return;
        }

        Location nearestLocation = null;
        double distance = 99999d;

        for (Location location : foodChests) {

            Material material = location.getBlock().getType();

            if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(material)) {

                Container block = (Container) location.getBlock().getState();

                // 不为空
                if (block.getInventory().isEmpty()) {
                    continue;
                }
                double locationDistance = location.distance(getNpc().getEntity().getLocation());

                // 冒泡排序
                if (locationDistance <= distance) {
                    nearestLocation = location;
                    distance = locationDistance;
                }

            }
        }

        if(distance > FOOD_CHEST_MAX_DISTANCE){
            // 如果食品箱子的距离大于 最大寻找的距离
            // 直接原地摆烂
            say("肚子好饿！附近都没有吃的了");
            return;
        }


        // 最近的食物箱子
        if (nearestLocation != null) {

            LoggerUtils.debug("找到箱子");
            // 如果距离大于3，就走过去
            if (distance > 3) {
                findPath(nearestLocation);
            }

            // 距离小于等于3，拿物品
            if (distance <= 3) {
                // 一次性拿20个

                // 扣减箱子里的物品，扣20个，不足20个有多少拿多少
                int i = ItemUtils.takeItemFromChest(nearestLocation, Material.BREAD, 20);

                if (i > 0) {

                    // 将扣减的数量添加到背包里
                    for (int count = 0; count < i; count++) {
                        addItemToFoodBag(new ItemStack(Material.BREAD));
                    }

                }
            }
        }

    }

    /**
     * 获取箱子当中的全部物品
     *
     * @param location
     * @return
     */
    private List<ItemStack> getChest(Location location) {

        List<ItemStack> results = new ArrayList<>();

        Block block = location.getBlock();

        if (ItemUtils.SUITABLE_CONTAINER_TYPES.contains(block.getType())) {
            Container chest = (Container) block.getState();
            Inventory blockInventory = chest.getInventory();

            @NonNull ItemStack[] contents = blockInventory.getContents();

            results = new ArrayList<>(Arrays.asList(contents));
        }

        return results;
    }

    /**
     * 吃饱了就回血
     */
    private void fullStomach() {

        if (!isAlive()) {
            return;
        }

        LivingEntity entity = (LivingEntity) npc.getEntity();

        if (getHunger() >= 20) {
            //自动回血
            if (entity.getHealth() < MAX_HEALTH) {

                if (MAX_HEALTH - entity.getHealth() >= 1) {
                    entity.setHealth(entity.getHealth() + 1);
                } else {
                    entity.setHealth(MAX_HEALTH);
                }
            }
        }

        //减少饱食度
        if (HUNGER_DELAY_COUNT < HUNGER_DELAY) {
            if (npc.getNavigator().isNavigating()) {
                HUNGER_DELAY_COUNT = HUNGER_DELAY_COUNT + 2;
            } else {
                HUNGER_DELAY_COUNT++;
            }

        }else{
            if(getHunger() > 0){
                setHunger(getHunger() - HUNGER_COST);
                HUNGER_DELAY_COUNT = 0;
            }
        }

        if(getHunger() <= 0){
            if(entity.getHealth() >= 1){
                // 自动造成伤害
                entity.damage(HUNGER_DAMAGE);
                if(!isAlive() && entity.getHealth() <= 0d){
                    // 生命值归零，死亡，饿死的情况下，可以自动报废建筑
                    LoggerUtils.debug("NPC 饿死了");
                }
            }
        }

        updateName();
    }

    /**
     * 穿衣服
     * todo 需要优化选择更好的物品装备
     */
    @Override
    public void wearClothes() {

        if (!isAlive()) {
            return;
        }

        Inventory backpack = getCoiNpc().getInventory();
        LivingEntity entity = (LivingEntity) npc.getEntity();

        Iterator<ItemStack> iterator = backpack.iterator();

        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            //头盔
            if (itemStack == null) continue;
            if (itemStack.getType() == Material.CHAINMAIL_HELMET
                    || itemStack.getType() == Material.DIAMOND_HELMET
                    || itemStack.getType() == Material.GOLDEN_HELMET
                    || itemStack.getType() == Material.IRON_HELMET
                    || itemStack.getType() == Material.LEATHER_HELMET
                    || itemStack.getType() == Material.NETHERITE_HELMET
                    || itemStack.getType() == Material.TURTLE_HELMET
            ) {
                if (itemStack.getType().equals(Material.LEATHER_HELMET)) {
                    // 皮革的，就换成小队颜色
                    if (getCoiNpc().getTeam().getType().getLeatherColor() != null) {
                        ItemUtils.changeColorForLeather(itemStack, getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }
                entity.getEquipment().setHelmet(itemStack);
                backpack.remove(itemStack);
                LoggerUtils.debug("NPC穿上了头盔");
                say("这可是个好东西啊，脑袋保住了");
                continue;
            }

            //胸甲
            if (itemStack.getType() == Material.CHAINMAIL_CHESTPLATE
                    || itemStack.getType() == Material.DIAMOND_CHESTPLATE
                    || itemStack.getType() == Material.GOLDEN_CHESTPLATE
                    || itemStack.getType() == Material.IRON_CHESTPLATE
                    || itemStack.getType() == Material.LEATHER_CHESTPLATE
                    || itemStack.getType() == Material.NETHERITE_CHESTPLATE
            ) {

                if (itemStack.getType().equals(Material.LEATHER_CHESTPLATE)) {
                    // 皮革的，就换成小队颜色
                    if (getCoiNpc().getTeam().getType().getLeatherColor() != null) {
                        ItemUtils.changeColorForLeather(itemStack, getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }
                entity.getEquipment().setChestplate(itemStack);
                backpack.remove(itemStack);
                LoggerUtils.debug("NPC穿上了胸甲");
                say("这是什么宝贝，胸罩么");
                continue;
            }

            //裤子
            if (itemStack.getType() == Material.LEATHER_LEGGINGS
                    || itemStack.getType() == Material.CHAINMAIL_LEGGINGS
                    || itemStack.getType() == Material.DIAMOND_LEGGINGS
                    || itemStack.getType() == Material.GOLDEN_LEGGINGS
                    || itemStack.getType() == Material.IRON_LEGGINGS
                    || itemStack.getType() == Material.NETHERITE_LEGGINGS
            ) {

                if (itemStack.getType().equals(Material.LEATHER_LEGGINGS)) {
                    // 皮革的，就换成小队颜色
                    if (getCoiNpc().getTeam().getType().getLeatherColor() != null) {
                        ItemUtils.changeColorForLeather(itemStack, getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }
                entity.getEquipment().setLeggings(itemStack);
                backpack.remove(itemStack);
                LoggerUtils.debug("NPC穿上了裤子");
                say("这难道就是皇帝的丝袜么");
                continue;
            }

            //靴子
            if (itemStack.getType() == Material.CHAINMAIL_BOOTS
                    || itemStack.getType() == Material.DIAMOND_BOOTS
                    || itemStack.getType() == Material.GOLDEN_BOOTS
                    || itemStack.getType() == Material.IRON_BOOTS
                    || itemStack.getType() == Material.LEATHER_BOOTS
                    || itemStack.getType() == Material.NETHERITE_BOOTS
            ) {

                if (itemStack.getType().equals(Material.LEATHER_BOOTS)) {
                    // 皮革的，就换成小队颜色
                    if (getCoiNpc().getTeam().getType().getLeatherColor() != null) {
                        ItemUtils.changeColorForLeather(itemStack, getCoiNpc().getTeam().getType().getLeatherColor());
                    }
                }

                entity.getEquipment().setBoots(itemStack);
                backpack.remove(itemStack);
                LoggerUtils.debug("NPC穿上了鞋");
                say("英雄不能没有切尔西！");
                continue;
            }

            //武器或工具
            //剑
            if (itemStack.getType() == Material.DIAMOND_SWORD
                    || itemStack.getType() == Material.STONE_SWORD
                    || itemStack.getType() == Material.WOODEN_SWORD
                    || itemStack.getType() == Material.GOLDEN_SWORD
                    || itemStack.getType() == Material.IRON_SWORD
                    || itemStack.getType() == Material.NETHERITE_SWORD
                    //斧头
                    || itemStack.getType() == Material.DIAMOND_AXE
                    || itemStack.getType() == Material.GOLDEN_AXE
                    || itemStack.getType() == Material.IRON_AXE
                    || itemStack.getType() == Material.NETHERITE_AXE
                    || itemStack.getType() == Material.STONE_AXE
                    || itemStack.getType() == Material.WOODEN_AXE
                    //镐子
                    || itemStack.getType() == Material.DIAMOND_PICKAXE
                    || itemStack.getType() == Material.GOLDEN_PICKAXE
                    || itemStack.getType() == Material.IRON_PICKAXE
                    || itemStack.getType() == Material.NETHERITE_PICKAXE
                    || itemStack.getType() == Material.STONE_PICKAXE
                    || itemStack.getType() == Material.WOODEN_PICKAXE
                    //锄头
                    || itemStack.getType() == Material.DIAMOND_HOE
                    || itemStack.getType() == Material.GOLDEN_HOE
                    || itemStack.getType() == Material.IRON_HOE
                    || itemStack.getType() == Material.NETHERITE_HOE
                    || itemStack.getType() == Material.STONE_HOE
                    || itemStack.getType() == Material.WOODEN_HOE
            ) {
                if (entity.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
                    entity.getEquipment().setItemInMainHand(itemStack);
                    backpack.remove(itemStack);
                    LoggerUtils.debug("NPC装备了武器或工具");
                    say("这东西真不错，很趁手！");
                }
            }
        }


    }

    /**
     * 捡起地上的物品
     */
    @Override
    public void pickItems() {

        if (!isAlive()) {
            return;
        }

        if (!npc.isSpawned()) {
            return;
        }


        List<Entity> nearbyEntities = npc.getEntity().getNearbyEntities(10, 2, 10);

        if (!nearbyEntities.isEmpty() && (targetItem == null || targetItem.isDead())) {
            for (Entity entity : nearbyEntities) {
                if (entity != null) {
                    if (entity.getType() == EntityType.DROPPED_ITEM) {


                        Item item = (Item) entity;
                        Set<String> picks = getCoiNpc().getPickItemMaterials();
                        if (picks != null && picks.size() > 0 && !ignoredItems.asMap().containsKey(item)) {
                            if (picks.contains(item.getItemStack().getType().toString()) && InventoryUtils.canInventoryHoldItem(getCoiNpc().getInventory(), item.getItemStack())) {
                                targetItem = item;
                                break;
                            } else {
                                ignoredItems.put(item, item);
                            }
                        }
                    }
                }

            }
        }

        if (targetItem != null && !targetItem.isDead()) {
            Set<String> picks = getCoiNpc().getPickItemMaterials();
            if (picks != null && picks.size() > 0) {


                if (picks.contains(targetItem.getItemStack().getType().toString())) {


                    if (targetItem.getLocation().distance(npc.getEntity().getLocation()) < 3) {

                        if (InventoryUtils.canInventoryHoldItem(coiNpc.getInventory(), targetItem.getItemStack())) {

                            addItemToInventory(targetItem.getItemStack());
                            targetItem.remove();
                        } else {
                            ignoredItems.put(targetItem, targetItem);
                            targetItem = null;
                        }
                    }


                } else {
                    findPath(targetItem.getLocation());
                }


            }
        }
    }

    /**
     * 将物品添加到背包里
     *
     * @param item
     */
    protected void addItemToInventory(ItemStack item) {
        if (item != null) {
            Inventory backpack = getCoiNpc().getInventory();
            if (!InventoryUtils.canInventoryHoldItem(backpack, item)) {

                npc.getEntity().getWorld().dropItem(npc.getEntity().getLocation(), item);
            } else {
                getCoiNpc().getInventory().addItem(item);
            }

        }
    }

    /**
     * 将物品添加到食品袋里
     *
     * @param item
     */
    private void addItemToFoodBag(ItemStack item) {
        addItemToInventory(item);
    }

    @Override
    public void move() {
        // 记录位置
        saveLastLocation();
        // 优先穿衣服
        wearClothes();
        // 捡起附近需要的物品 使用同步进程去做
        pickItems();
        // 吃饱了回血
        fullStomach();
        // 没吃饱就去吃
        eatFood();
        // 寻找食物
        findFood();
        // 检查死没死
        dead();
    }

    /**
     * 生成NPC
     */
    @Override
    public void spawn(Location location) {

        if (npc.isSpawned() && this.isSpawn) {
            LoggerUtils.log("NPC已spawn，无法再次生成");
            return;
        }

        // 更新NPC的名称
        updateName();

        npc.spawn(location);

        // 设置伪装
        if(getCoiNpc().getDisguise() != null){
            MobDisguise disguise = getCoiNpc().getDisguise();
            disguise.setEntity(npc.getEntity());
            disguise.startDisguise();
        }

        // 恢复血量和饱食度
        initEntityStatus();

        npc.setProtected(false);
        this.isSpawn = true;

        say("干活！干活！");
    }

    @Override
    public void despawn() {
        if (this.npc != null) {
            npc.despawn();
            if(getCoiNpc().getDisguise() != null){
                MobDisguise disguise = getCoiNpc().getDisguise();
                disguise.stopDisguise();
            }
            this.isSpawn = false;
        }
    }

    @Override
    public void remove() {
        isRemoved = true;
        despawn();
        npc.destroy();
    }

    /**
     * NPC是否还存活
     *
     * @return
     */
    @Override
    public boolean isAlive() {

        if (npc.getEntity() == null || npc.getEntity().isDead()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
    }

    @Override
    public Location getLocation() {

        if (isAlive()) {
            return npc.getEntity().getLocation();
        }
        return null;
    }

    /**
     * NPC死亡时调用本方法
     */
    public void dead() {

        // 判断是否还活着
        if (isAlive()) {
            return;
        }

        // 死亡掉落全部物资
        dropAllItems(getLastLocation());

        if (coiNpc.isCanRespawn()) {
            if (!isRespawning) {
                isRespawning = true;
                LoggerUtils.debug("NPC死亡了");
            }

            if (isRespawning) {
                RESPAWN_DELAY_COUNT++;
                if (RESPAWN_DELAY_COUNT == coiNpc.getRespawnDelay()) {
                    RESPAWN_DELAY_COUNT = 0;
                    isRespawning = false;

                    // 复活
                    spawn(coiNpc.getSpawnLocation());
                    LoggerUtils.debug("NPC复活了");
                }
            }
        }else{
            // 无法复活的，直接删除
            coiNpc.getNpc().remove();
        }

    }

    /**
     * NPC死亡触发物品全部掉落
     */
    public void dropAllItems(Location location) {

        if (location == null) {
            return;
        }

        Inventory inventory = getCoiNpc().getInventory();


        for (ItemStack next : inventory) {
            if (next != null) location.getWorld().dropItem(location, next);
        }

        // 清空缓存
        // getCoiNpc().setFoodBag(new ArrayList<>());
        getCoiNpc().getInventory().clear();

    }

    /**
     * 内部方法，获取NPC名称
     *
     * @return
     */
    private String getName() {

        String teamColor = "";

        // 名字改为小队颜色
        if (getCoiNpc().getTeam() != null) {
            teamColor = getCoiNpc().getTeam().getType().getColor();
        }

        String hungerColor = "&a";

        if (getHunger() >= 10) {
            hungerColor = "&a";
        } else if (getHunger() >= 5) {
            hungerColor = "&6";
        } else if (getHunger() < 5) {
            hungerColor = "&c";
        }

        // 名字组成 Lv.1 名称 20.0
        return LoggerUtils.replaceColor(teamColor + "Lv." + coiNpc.getLevel() + " " + coiNpc.getName() + " " + hungerColor + getHunger());
    }

    /**
     * 内部方法，更新NPC 名称
     */
    private void updateName() {

        // 名字变了再更新
        if (!this.npc.getName().equals(getName())) {
            this.npc.setName(getName());
        }

    }

    /**
     * 初始化NPC的参数
     */
    protected void initNpcAttributes(COINpc npcCreator) {

        // 恢复血量和饱食度
        initEntityStatus();

        // 如果背包未初始化
        if (npcCreator.getInventory() == null) {
            npcCreator.setInventory(GUIUtils.createNpcInventory(3));
        }


        // 设置NPC的名称使用 Hologram
        this.npc.setAlwaysUseNameHologram(true);

        this.npc.data().set(NPC.Metadata.KEEP_CHUNK_LOADED, true);
        // 初始化NPC的皮
        this.npc.data().set("player-skin-textures", npcCreator.getSkinTextures());
        this.npc.data().set("cached-skin-uuid-name", npcCreator.getSkinName());
        this.npc.data().set("player-skin-name", npcCreator.getSkinName());
        this.npc.data().set("player-skin-signature", npcCreator.getSkinSignature());
        this.npc.data().set("player-skin-use-latest-skin", false);
    }

    /**
     * 初始化生物状态
     */
    protected void initEntityStatus() {
        // 饥饿值默认20
        setHunger(DEFAULT_HUNGER);

        if (isAlive()) {
            // 满血
            LivingEntity entity = (LivingEntity) npc.getEntity();

            NpcAITask.runTask(this);

            entity.setMetadata("entityData", new EntityData(getCoiNpc()));

            entity.setHealth(MAX_HEALTH);
        }
    }

    /**
     * 获取 NPC 范围内的方块
     *
     * @param radius
     * @return
     */
    @Override
    public List<Block> getNearbyBlocks(double radius) {
        if (getNpc() == null) {
            return new ArrayList<>();
        }

        List<Block> list = new ArrayList<>();

        Location nowLoc = getNpc().getEntity().getLocation();
        double minX = nowLoc.getX() - radius;
        double maxX = nowLoc.getX() + radius;
        double minY = nowLoc.getY() - 1;
        double maxY = nowLoc.getY() + 3;
        double minZ = nowLoc.getZ() - radius;
        double maxZ = nowLoc.getZ() + radius;

        for (double x = minX; x < maxX; x++) {
            for (double y = minY; y < maxY; y++) {
                for (double z = minZ; z < maxZ; z++) {
                    Block blockAt = getNpc().getEntity().getWorld().getBlockAt(new Location(getNpc().getEntity().getWorld(), x, y, z));
                    if (blockAt.getType() != Material.AIR) {
                        list.add(blockAt);
                    }
                }
            }
        }

        //根据Y轴排序
        Collections.sort(list, Comparator.comparingDouble(Block::getY));
        //再次翻转，从高到低
        Collections.reverse(list);

        return list;
    }

    @Override
    public List<Entity> getNearByEntities(double radius) {

        if (!isAlive()) {
            return new ArrayList<>();
        }

        List<Entity> result = new ArrayList<>();

        List<Entity> nearbyEntities = npc.getEntity().getNearbyEntities(radius, radius, radius);

        if (!nearbyEntities.isEmpty()) {
            for (Entity entity : nearbyEntities) {
                if (entity != null) {
                    if (entity instanceof LivingEntity) {
                        // 普通生物
                        result.add(entity);
                    }
                }

            }
        }

        result.sort(Comparator.comparingDouble(b -> getLocation().distance(b.getLocation())));
        return result;
    }

    public NPC getNpc() {
        return npc;
    }

    public COINpc getCoiNpc() {
        return coiNpc;
    }

    public boolean isSpawn() {
        return isSpawn;
    }

    public boolean isCreated() {
        return isCreated;
    }

    public double getHunger() {
        return hunger;
    }

    /**
     * 获取NPC实时血量
     * @return
     */
    public double getHealth(){
        if(!isAlive()){
            return 0;
        }
        LivingEntity entity = (LivingEntity) npc.getEntity();
        return entity.getHealth();
    }

    /**
     * 设置NPC实时血量
     * @param health
     */
    public void setHealth(double health){
        if(!isAlive()){
            return;
        }
        LivingEntity entity = (LivingEntity) npc.getEntity();
        entity.setHealth(health);
    }

    /**
     * 内部方法，设置NPC的饥饿度
     *
     * @param hunger
     */
    public void setHunger(double hunger) {
        this.hunger = hunger;
    }

    /**
     * 获取NPC最大血量
     *
     * @return
     */
    private double getMaxHealth() {
        return MAX_HEALTH;
    }

    /**
     * 饿的不能继续工作了
     *
     * @return
     */
    public boolean isTooHungryToWork() {

        if (isHungry) {
            // 食物背包里的物品为空

            return true;

        }

        return false;
    }

    /**
     * 最后一次保存的位置
     *
     * @return
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    public void saveLastLocation() {

        if (!isAlive()) {
            return;
        }

        // 记录NPC所在位置
        this.lastLocation = getNpc().getEntity().getLocation();
    }

}
