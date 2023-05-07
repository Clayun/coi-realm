package com.mcylm.coi.realm.tools.attack.target.impl;

import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class EntityTarget implements Target {

    private LivingEntity entity;

    @Override
    public TargetType getType() {
        return TargetType.ENTITY;
    }

    @Override
    public @NotNull Location getTargetLocation() {
        return entity.getLocation();
    }

    @Override
    public boolean isDead() {
        return entity.isDead();
    }
}
