package com.mcylm.coi.realm.player;

import com.mcylm.coi.realm.model.COISkin;
import com.mcylm.coi.realm.player.settings.PlayerSettings;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;

public class COIPlayer {

    private final Player player;
    @Getter
    private Set<Target> selectedTargets = new HashSet<>();

    @Getter
    private AttackTeam attackTeam;
    // 死亡次数
    @Setter
    @Getter
    private int deathCount = 0;

    // 是否死亡复活中
    @Setter
    @Getter
    private boolean death = false;

    // 上一次摧毁建筑的时间
    @Setter
    @Getter
    private LocalDateTime lastDamageBuilding;

    @Setter
    @Getter
    @Nullable
    private Target attackedTarget;

    @Getter
    private PlayerSettings settings = new PlayerSettings();

    // 玩家已选择并使用的建筑皮肤
    @Setter
    @Getter
    private HashMap<String,COISkin> selectedSkins;


    public Player getBukkitPlayer() {
        return player;
    }

    public COIPlayer(Player player){
        this.player = player;
        this.attackTeam = new AttackTeam().setCommander(player).setMembers(new ArrayList<>());
        this.lastDamageBuilding = null;
        this.selectedSkins = new HashMap<>();

        // TODO 玩家设置部分待开发
    }

    /**
     * 获取死亡复活时间
     * @return
     */
    public int getResurrectionCountdown(){

        if(deathCount >= 10){
            return 10;
        }else{
            if(deathCount < 5){
                return 5;
            }
            return deathCount;
        }

    }
}
