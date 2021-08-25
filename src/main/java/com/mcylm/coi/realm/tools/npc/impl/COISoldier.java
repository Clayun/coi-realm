package com.mcylm.coi.realm.tools.npc.impl;

import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.utils.FormationUtils;
import net.citizensnpcs.api.ai.tree.Behavior;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * 战士
 * 拥有多人自动编排能力，自发的组成阵容
 * 会对敌对阵营的建筑进行破坏，并主动攻击敌对阵营玩家
 * 主动跟随阵营内拥有将军令的玩家
 */
public class COISoldier extends COIHuman{

    // 周围发现敌人，进入战斗模式
    private boolean fighting = false;

    // NPC所属阵型编排
    private List<List<Integer>> formats;

    // NPC编排编号，可跟GUI联动
    private Integer npcNumber;

    public COISoldier(COISoldierCreator npcCreator) {
        super(npcCreator);

        if(npcCreator.getNpcNumber() != null){
            this.npcNumber = npcCreator.getNpcNumber();
        }
        if(npcCreator.getFormats() != null){
            this.formats = npcCreator.getFormats();
        }
    }

    /**
     * 警戒
     */
    private void alert(){


        List<Entity> nearByEntities = getNearByEntities(getCoiNpc().getAlertRadius());

        if(nearByEntities.isEmpty()){
            return;
        }

        // 是否需要开启战斗模式
        boolean needFight = false;

        for(Entity entity : nearByEntities){

            if(getCoiNpc().getEnemyPlayers() != null
                    && !getCoiNpc().getEnemyPlayers().isEmpty()){
                if(entity.getType().equals(EntityType.PLAYER)){
                    Player player = (Player) entity;

                    if(getCoiNpc().getEnemyPlayers().contains(player.getName())){
                        // 找到敌对玩家，进入战斗状态
                        needFight = true;
                        // 发动攻击
                        attack(entity);
                        break;
                    }

                }
            }

            if(getCoiNpc().getEnemyEntities()!= null
                    && !getCoiNpc().getEnemyEntities().isEmpty()){

                if(getCoiNpc().getEnemyEntities().contains(entity.getType())){
                    // 找到敌对生物，进入战斗状态
                    needFight = true;
                    // 发动攻击
                    // 如果NPC设置了主动攻击，就开始战斗
                    if(getCoiNpc().isAggressive()){
                        attack(entity);
                    }
                    break;
                }
            }


        }

        fighting = needFight;

    }

    /**
     * 攻击实体
     * @param entity
     */
    private void attack(Entity entity){
        
        if(getNpc().getEntity().getLocation().distance(entity.getLocation()) <= 3){

            // 挥动手
            ((LivingEntity)getNpc().getEntity()).swingMainHand();

            // 对生物体直接产生伤害
            Random rand = new Random();

            // 在攻击伤害范围内，随机产生伤害
            double damage = rand.nextInt((int) ((getCoiNpc().getMaxDamage() + 1) - getCoiNpc().getMinDamage())) + getCoiNpc().getMinDamage();
            ((LivingEntity)entity).damage(damage);

        }else{

            // 追击对方
            findPath(entity.getLocation());
        }
    }


    /**
     * 自动计算NPC的位置，组成阵型
     * 如果没有阵型，就主动跟随玩家
     */
    private void formation(){

        if(fighting){
            return;
        }

        // 如果没有跟随的玩家，就原地待命
        if(StringUtils.isBlank(getCoiNpc().getFollowPlayerName())){
            return;
        }

        Player player = Bukkit.getPlayer(getCoiNpc().getFollowPlayerName());

        if(player != null && player.isOnline()){

            if(npcNumber == null
                || formats == null){
                // 如果没有初始化队形，就跟随玩家
                findPath(player.getLocation());

            }else{
                List<Location> locations = FormationUtils.calculateFormation(player.getLocation(), formats);

                if(locations.size() >= npcNumber){
                    int index = npcNumber - 1;

                    // NPC编排所在位置
                    Location location = locations.get(index);

                    walk(location,player.getEyeLocation());
                }
            }


        }

    }

    /**
     * 行军寻路
     * @param location
     * @param faceLocation
     */
    public void walk(Location location,Location faceLocation) {
        if(getNpc() == null){
            return;
        }

        if(!getNpc().isSpawned()){
            return;
        }

        getNpc().faceLocation(faceLocation);
        getNpc().getNavigator().setTarget(location);
    }

    /**
     * 更换NPC跟随的玩家
     * @param newFollowPlayer
     */
    public void changeFollowPlayer(String newFollowPlayer){
        getCoiNpc().setFollowPlayerName(newFollowPlayer);
    }

    /**
     * 更换阵型
     * @param formats
     */
    public void updateFormats(List<List<Integer>> formats){
        this.formats = formats;
    }

    /**
     * 更换NPC的编号
     * @param number
     */
    public void setNumber(Integer number){
        this.npcNumber = number;
    }

    @Override
    public void move(){
        super.move();

        // 布阵
        formation();

        //警戒周围
        alert();
    }


}
