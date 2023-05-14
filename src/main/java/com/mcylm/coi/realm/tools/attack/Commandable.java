package com.mcylm.coi.realm.tools.attack;


import com.mcylm.coi.realm.player.COIPlayer;
import org.bukkit.entity.Player;

public interface Commandable extends DamageableAI {

    void setCommander(COIPlayer player);

    COIPlayer getCommander();

    void setGoal(AttackGoal goal);

    AttackGoal getGoal();

}
