package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.utils.FormationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 战士
 * 拥有多人自动编排能力，自发的组成阵容
 * 会对敌对阵营的建筑进行破坏，并主动攻击敌对阵营玩家
 * 主动跟随阵营内拥有将军令的玩家
 */
public class COISoldier extends COIHuman{

    // NPC编排编号，可跟GUI联动
    private int npcNumber;

    public COISoldier(COISoldierCreator npcCreator) {
        super(npcCreator);
        this.npcNumber = npcCreator.getNpcNumber();
    }

    /**
     * 自动计算NPC的位置
     */
    public void formation(){
        Player player = Bukkit.getPlayer(getCoiNpc().getFollowPlayerName());

        if(player != null && player.isOnline()){
            List<Location> locations = FormationUtils.calculateFormation(player.getLocation(), FormationUtils.customFormat());

            if(locations.size() >= npcNumber){
                int index = npcNumber - 1;

                // NPC编排所在位置
                Location location = locations.get(index);

                walk(location,player.getEyeLocation());
            }
        }

    }

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

    @Override
    public void move(){
        super.move();

        // 布阵
        formation();
    }


}
