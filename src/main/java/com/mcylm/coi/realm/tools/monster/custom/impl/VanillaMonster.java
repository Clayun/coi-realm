package com.mcylm.coi.realm.tools.monster.custom.impl;

import com.mcylm.coi.realm.tools.monster.custom.CustomMonster;
import lombok.AllArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

@AllArgsConstructor
public class VanillaMonster implements CustomMonster {
    private EntityType type;

    @Override
    public void spawn(Monster monster) {

    }

    @Override
    public EntityType getType() {
        return type;
    }


}
