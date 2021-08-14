package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.runnable.TaskRunnable;
import com.mcylm.coi.realm.utils.LoggerUtils;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 工人
 * 相较于普通AI增加了拆方块的功能，可以在此基础上细分工种
 */
public class COIWorker extends COIHuman{

    private boolean isBreaking = false;
    // 待拆除的方块
    private List<Block> targetBlocks;

    public COIWorker(COINpc npcCreator) {
        super(npcCreator);
        // 初始化NPC待拆方块
        this.targetBlocks = new ArrayList<>();
    }

    /**
     * 工人会清空背包内容
     * @param coiNpc
     * @param respawn 是否重新生成，重新生成会清空背包
     * @return
     */
    public COIHuman update(COINpc coiNpc,boolean respawn) {

        COIHuman update = super.update(coiNpc, respawn);

        if(update != null && respawn){
            // 初始化NPC待拆方块
            this.targetBlocks = new ArrayList<>();
        }

        return update;
    }

    /**
     * 内部方法，获取 NPC 范围内的方块
     * @param radius
     * @return
     */
    private List<Block> getNearbyBlocks(double radius) {
        if(getNpc() == null){
            return new ArrayList<>();
        }

        List<Block> list = new ArrayList<>();

        Location nowLoc = getNpc().getEntity().getLocation();
        double minX = nowLoc.getX() - radius;
        double maxX = nowLoc.getX() + radius;
        double minY = nowLoc.getY()-1;
        double maxY = nowLoc.getY()+3;
        double minZ = nowLoc.getZ() - radius;
        double maxZ = nowLoc.getZ() + radius;

        for(double x = minX;x < maxX; x ++){
            for(double y = minY;y < maxY; y ++){
                for(double z = minZ;z < maxZ; z ++){
                    Block blockAt = getNpc().getEntity().getWorld().getBlockAt(new Location(getNpc().getEntity().getWorld(), x, y, z));
                    if(blockAt.getType() != Material.AIR){
                        list.add(blockAt);
                    }
                }
            }
        }

        return list;
    }

    /**
     * 添加需要拆除的方块到 NPC 的缓冲区
     * @return
     */
    private void addBlockTargets() {

        List<Block> locations = new ArrayList<>();

        // NPC的目标方块类型
        Set<String> blocks =  getCoiNpc().getBreakBlockMaterials();

        // 需要拆除的方块目标结果集
        List<Block> blocksNearByNpc = getNearbyBlocks(1.5);

        if(blocks != null){
            for(String blockName : blocks){
                for(Block block : blocksNearByNpc){

                    // 比对获取到的附近的方块是否是需要拆除的
                    Material material = Material.getMaterial(blockName);
                    if(material != null){
                        if(material == block.getBlockData().getMaterial()){
                            locations.add(block);
                        }
                    }
                }

            }
        }

        // 添加到缓冲区
        this.targetBlocks.addAll(locations);

    }

    /**
     * 自动前往需要拆除的方块的位置
     */
    public void findAndBreakBlock(){
        //优先拆方块
        List<Block> targetBlocks = this.targetBlocks;

        Block targetBlock = null;

        if(targetBlocks != null && targetBlocks.size() > 0){

            Iterator<Block> iterator = targetBlocks.iterator();
            while (iterator.hasNext()) {
                targetBlock = iterator.next();
                if(targetBlock != null){

                    Location clone = targetBlock.getLocation().clone();
                    clone.setY(clone.getY()+1);

                    if(targetBlock.getWorld().getBlockAt(targetBlock.getLocation()).getType() == Material.AIR){
                        iterator.remove();
                    }else{
                        if (getNpc().getEntity().getLocation().distance(targetBlock.getLocation()) <= 3) {

                            if (!isBreaking) {

                                LivingEntity entity = (LivingEntity) getNpc().getEntity();
                                BlockBreaker.BlockBreakerConfiguration blockBreakerConfiguration = new BlockBreaker.BlockBreakerConfiguration();
                                blockBreakerConfiguration.radius(5);
                                blockBreakerConfiguration.item(entity.getEquipment().getItemInMainHand());
                                blockBreakerConfiguration.callback(
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
//                                                LoggerUtils.log("拆除完毕");
                                                isBreaking = false;
                                            }
                                        }
                                );
                                isBreaking = true;
                                BlockBreaker breaker = getNpc().getBlockBreaker(targetBlock, blockBreakerConfiguration);
                                if (breaker.shouldExecute()) {
                                    TaskRunnable run = new TaskRunnable(breaker);
                                    run.setTaskId(Bukkit.getScheduler().scheduleSyncRepeatingTask(Entry.getInstance(), run, 0, 1));
                                }
                            }


                        } else {

                            getNpc().faceLocation(targetBlock.getLocation());
                            findPath(targetBlock.getLocation());

                        }
                    }
                }
            }
        }else{
            addBlockTargets();
        }
    }

    public void move() {
        super.move();
        //找可拆除的去拆
        findAndBreakBlock();
    }

}
