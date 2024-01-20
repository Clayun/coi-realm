package com.mcylm.coi.realm.tools.monster.custom;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

public interface CustomMonster {
    void spawn(Monster monster);

    EntityType getType();

}
