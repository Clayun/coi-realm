package com.mcylm.coi.realm.tools.attack;


import org.bukkit.entity.LivingEntity;

public interface Commandable extends DamageableAI {

    void setCommander(LivingEntity entity);

    LivingEntity getCommander();

    void setGoal(AttackGoal goal);

    AttackGoal getGoal();

}
