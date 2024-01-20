package com.mcylm.coi.realm.tools.monster.custom.impl;

import com.mcylm.coi.realm.tools.monster.custom.CustomMonster;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

import java.util.UUID;

public class GiantMonster implements CustomMonster {
    @Override
    public void spawn(Monster monster) {
        DisguiseAPI.disguiseEntity(monster, new MobDisguise(DisguiseType.GIANT));
        monster.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .addModifier(new AttributeModifier("health", 100, AttributeModifier.Operation.ADD_NUMBER));
        monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .addModifier(new AttributeModifier("speed", 0.7, AttributeModifier.Operation.ADD_SCALAR));

    }

    @Override
    public EntityType getType() {
        return EntityType.RAVAGER;
    }
}
