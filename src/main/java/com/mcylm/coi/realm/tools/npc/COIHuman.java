package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.utils.LoggerUtils;
import me.lucko.helper.Schedulers;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 所有AI的父类，每个
 */
public class COIHuman implements COIAI{

    // CitizensNPC 实例
    private NPC npc;
    // COIAI 实例
    private COINpc coiNpc;
    // NPC饱食度
    private double hunger;
    // NPC是否已生成
    private boolean isSpawn = false;
    // Citizens 实例是否已创建
    private boolean isCreated = false;
    // 饥饿度计算值间隔
    private static final int HUNGER_DELAY = 20;
    // 饥饿度计数值
    private int HUNGER_DELAY_COUNT = 0;

    // 构造NPC
    public COIHuman(COINpc npcCreator) {
        create(npcCreator);
    }

    /**
     * 创建 AI NPC 实例，万物之始
     * @param npcCreator
     * @return
     */
    @Override
    public COIHuman create(COINpc npcCreator) {

        if(npcCreator == null){
            LoggerUtils.log("npc创建对象为空，无法生成NPC");
            return null;
        }

        this.coiNpc = npcCreator;
        // 创建 CitizensNPC 实例
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, getName());
        this.isCreated = true;

        initNpcAttributes(npcCreator);

        return this;
    }

    /**
     * 更新 NPC属性
     * @param coiNpc
     * @param respawn 是否重新生成，重新生成会清空背包
     * @return
     */
    @Override
    public COIHuman update(COINpc coiNpc,boolean respawn) {

        if(this.coiNpc == null || !isCreated){
            LoggerUtils.log("npc从未创建，无法更新");
            return null;
        }

        // 临时缓存老的NPC数据
        COINpc oldNpc = this.coiNpc;

        // 给NPC赋值新的数据
        this.coiNpc = coiNpc;

        if(respawn){
            // 清空背包
            this.coiNpc.setInventory(new ArrayList<>());

            // 如果NPC还存活
            if(isAlive()){
                // NPC的当前位置
                Location entityLocation = this.npc.getEntity().getLocation();
                // NPC重新生成
                despawn();
                spawn(entityLocation);
            }else{
                // 重生
                despawn();
                spawn(coiNpc.getSpawnLocation());
            }

        }else
            // 还原背包
            this.coiNpc.setInventory(oldNpc.getInventory());

        initNpcAttributes(this.coiNpc);

        return this;
    }

    /**
     * 自动寻路，默认的算法
     * @param location
     */
    @Override
    public void findPath(Location location) {
        if(npc == null){
            return;
        }

        if(!npc.isSpawned()){
            return;
        }

        npc.faceLocation(location);
        npc.getNavigator().setTarget(location);
    }

    /**
     * 控制 NPC 说话
     * @param message
     */
    @Override
    public void say(String message) {
        LoggerUtils.broadcastMessage(getName()+"："+message);
    }

    /**
     * 吃背包里的食物，恢复饥饿度，从而让血量恢复
     */
    @Override
    public void eatFood() {

        LivingEntity entity = (LivingEntity) npc.getEntity();

        if(getHunger() >= 20){
            return;
        }

        List<ItemStack> backpack = getCoiNpc().getInventory();

        // 在背包里找吃的
        if(!backpack.isEmpty()){
            Iterator<ItemStack> iterator = backpack.iterator();
            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                if(item != null){
                    List<String> npcFoods = Entry.getNpcFoods();
                    for(String foodName : npcFoods){
                        Material material = Material.getMaterial(foodName);

                        if(material != null){
                            if(item.getType() == material){
                                if(item.getAmount() >= 1){
                                    item.setAmount(item.getAmount() - 1);
                                    ItemStack cache = null;
                                    if(entity.getEquipment().getItemInMainHand() != null){
                                        cache = entity.getEquipment().getItemInMainHand();
                                    }
                                    entity.getEquipment().setItemInMainHand(item);
                                    setHunger(getHunger() + 1);
                                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EAT,100,2);
                                    entity.getEquipment().setItemInMainHand(cache);
                                    return;
                                }else{
                                    iterator.remove();
                                }
                            }
                        }
                    }


                }

            }
        }
    }

    /**
     * 生物本能
     * 内部方法，检查饥饿度小于一定值
     * 生物要开启兽性模式，为了生存而战，寻找一切可以吃的食物
     * 砍杀生物获取吃的
     */
    private void checkHunger(){

        LivingEntity entity = (LivingEntity) npc.getEntity();

        if(getHunger() <= 5){
            return;
        }

        //todo 猎杀生物
    }

    /**
     * 吃饱了就回血
     */
    private void fullStomach(){

        LivingEntity entity = (LivingEntity) npc.getEntity();

        if(getHunger() >= 20){
            //自动回血
            if(entity.getHealth() < entity.getMaxHealth()){

                if(entity.getMaxHealth() - entity.getHealth() >= 1){
                    entity.setHealth(entity.getHealth() + 1);
                }else{
                    entity.setHealth(entity.getMaxHealth());
                }
            }
        }

        //减少饱食度
        if(HUNGER_DELAY_COUNT < HUNGER_DELAY){
            if(npc.getNavigator().isNavigating()){
                HUNGER_DELAY_COUNT = HUNGER_DELAY_COUNT + 2;
            }else{
                HUNGER_DELAY_COUNT ++;
            }

        }else{
            if(getHunger() > 0){
                setHunger(getHunger() - 1);
                HUNGER_DELAY_COUNT = 0;
            }
        }

        if(getHunger() <= 0){
            if(entity.getHealth() > 1){
                entity.setHealth(entity.getHealth() - 0.5);
            }
        }

        updateName();

        if(entity.getHealth() <= 5 && getHunger() <= 5){
            say("&c我现在又饿又虚弱，我需要食物");
        }else if(entity.getHealth() <= 5){
            say("&6我需要治疗");
        }else if(getHunger() <= 5){
            say("&6我现在非常饥饿，我需要食物");
        }
    }

    /**
     * 穿衣服
     * todo 需要优化选择更好的物品装备
     */
    @Override
    public void wearClothes() {
        List<ItemStack> backpack = getCoiNpc().getInventory();
        LivingEntity entity = (LivingEntity) npc.getEntity();

        Iterator<ItemStack> iterator = backpack.iterator();

        while(iterator.hasNext()){
            ItemStack itemStack = iterator.next();
            //头盔
            if(itemStack.getType() == Material.CHAINMAIL_HELMET
                    || itemStack.getType() == Material.DIAMOND_HELMET
                    || itemStack.getType() == Material.GOLDEN_HELMET
                    || itemStack.getType() == Material.IRON_HELMET
                    || itemStack.getType() == Material.LEATHER_HELMET
                    || itemStack.getType() == Material.NETHERITE_HELMET
                    || itemStack.getType() == Material.TURTLE_HELMET
            ){
                if(entity.getEquipment().getHelmet() == null){
                    entity.getEquipment().setHelmet(itemStack);
                    iterator.remove();
                }
            }

            //胸甲
            if(itemStack.getType() == Material.CHAINMAIL_CHESTPLATE
                    || itemStack.getType() == Material.DIAMOND_CHESTPLATE
                    || itemStack.getType() == Material.GOLDEN_CHESTPLATE
                    || itemStack.getType() == Material.IRON_CHESTPLATE
                    || itemStack.getType() == Material.LEATHER_CHESTPLATE
                    || itemStack.getType() == Material.NETHERITE_CHESTPLATE
            ){
                if(entity.getEquipment().getChestplate() == null){
                    entity.getEquipment().setChestplate(itemStack);
                    iterator.remove();
                }

            }

            //裤子
            if(itemStack.getType() == Material.LEATHER_LEGGINGS
                    || itemStack.getType() == Material.CHAINMAIL_LEGGINGS
                    || itemStack.getType() == Material.DIAMOND_LEGGINGS
                    || itemStack.getType() == Material.GOLDEN_LEGGINGS
                    || itemStack.getType() == Material.IRON_LEGGINGS
                    || itemStack.getType() == Material.NETHERITE_LEGGINGS
            ){
                if(entity.getEquipment().getLeggings() == null){
                    entity.getEquipment().setLeggings(itemStack);
                    iterator.remove();
                }

            }

            //靴子
            if(itemStack.getType() == Material.CHAINMAIL_BOOTS
                    || itemStack.getType() == Material.DIAMOND_BOOTS
                    || itemStack.getType() == Material.GOLDEN_BOOTS
                    || itemStack.getType() == Material.IRON_BOOTS
                    || itemStack.getType() == Material.LEATHER_BOOTS
                    || itemStack.getType() == Material.NETHERITE_BOOTS
            ){
                if(entity.getEquipment().getBoots() == null){
                    entity.getEquipment().setBoots(itemStack);
                    iterator.remove();
                }

            }

            //武器或工具
            //剑
            if(itemStack.getType() == Material.DIAMOND_SWORD
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
            ){
                if(entity.getEquipment().getItemInMainHand() != null){
                    entity.getEquipment().setItemInMainHand(itemStack);
                    iterator.remove();
                }

            }
        }
    }

    /**
     * 捡起地上的物品
     */
    private void pickItems(){

        if(!npc.isSpawned()){
            return;
        }

        if(getCoiNpc().getInventory().size() == 45){
            return;
        }

        List<Entity> nearbyEntities = npc.getEntity().getNearbyEntities(10, 2, 10);

        if(nearbyEntities != null && nearbyEntities.size() > 0){
            for(Entity entity : nearbyEntities){
                if (entity instanceof Item) {
                    Item i = (Item) entity;

                    Set<String> picks = getCoiNpc().getPickItemMaterials();
                    if(picks != null && picks.size() > 0){

                        for(String pickItemName : picks){
                            Material material = Material.getMaterial(pickItemName);
                            if(material != null){
                                if(i.getItemStack().getType() == material) {
                                    if(i.getLocation().distance(npc.getEntity().getLocation()) < 3){
                                        addItemToInventory(i.getItemStack());
                                        i.remove();
                                    }else{
                                        findPath(i.getLocation());
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * 将物品添加到背包里
     * @param item
     */
    private void addItemToInventory(ItemStack item){
        if(item != null){
            List<ItemStack> backpack = getCoiNpc().getInventory();
            if(backpack.size() >= 45){

                say("我的背包满了，这些东西装不下了");
                npc.getEntity().getWorld().dropItem(npc.getEntity().getLocation(),item);
            }else{
                getCoiNpc().getInventory().add(item);
            }

        }
    }

    @Override
    public void move() {
        //优先穿衣服
        wearClothes();
        //捡起附近需要的物品 使用同步进程去做
        Schedulers.sync().run(() -> pickItems());
        //吃饱了回血
        fullStomach();
        //没吃饱就去吃
        eatFood();
    }

    /**
     * 生成NPC
     */
    @Override
    public void spawn(Location location) {

        if(npc.isSpawned() && this.isSpawn){
            LoggerUtils.log("NPC已spawn，无法再次生成");
            return;
        }

        // 更新NPC的名称
        updateName();
        npc.spawn(location);
        npc.setProtected(false);
        this.isSpawn = true;
    }

    @Override
    public void despawn() {
        if(this.npc != null){
            npc.despawn();
            this.isSpawn = false;
        }
    }

    /**
     * NPC是否还存活
     * @return
     */
    @Override
    public boolean isAlive() {

        if(npc.getEntity().isDead()){
            return false;
        }

        return true;
    }

    /**
     * NPC死亡时调用本方法
     */
    @Override
    public void dead() {

    }

    /**
     * 内部方法，获取NPC名称
     * @return
     */
    private String getName(){
        return LoggerUtils.replaceColor("[LV."+coiNpc.getLevel()+"]"+coiNpc.getName() + " "+getHunger());
    }

    /**
     * 内部方法，更新NPC 名称
     */
    private void updateName(){
        this.npc.setName(getName());
    }

    /**
     * 初始化NPC的参数
     */
    private void initNpcAttributes(COINpc npcCreator){

        // 饥饿值默认20
        hunger = 20;

        // 设置NPC的名称使用 Hologram
        this.npc.setAlwaysUseNameHologram(true);

        // 初始化NPC的皮肤
        this.npc.data().set("player-skin-textures",npcCreator.getSkinTextures());
        this.npc.data().set("cached-skin-uuid-name",npcCreator.getSkinName());
        this.npc.data().set("player-skin-name",npcCreator.getSkinName());
        this.npc.data().set("player-skin-signature",npcCreator.getSkinSignature());
        this.npc.data().set("player-skin-use-latest-skin",false);

        // 设置NPC的敌对生物
        // 如果设置了主动攻击，则NPC会主动攻击敌对生物
        Set<EntityType> targets = npcCreator.getEnemyEntities();
        if(targets != null){
            TargetNearbyEntityGoal targetNearbyEntityGoal = TargetNearbyEntityGoal.builder(npc)
                    .targets(targets).aggressive(npcCreator.isAggressive()).radius(npcCreator.getAlertRadius()).build();
            this.npc.getDefaultGoalController().addGoal(targetNearbyEntityGoal,1);
        }
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
     * 内部方法，设置NPC的饥饿度
     * @param hunger
     */
    private void setHunger(double hunger) {
        this.hunger = hunger;
    }
}
