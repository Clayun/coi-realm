package com.mcylm.coi.realm.tools.attack.target;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public abstract class Target {

    @Getter
    @Setter
    private int targetLevel;

    public Target(int p) {
        this.targetLevel = p;
    }

    public abstract TargetType getType();

    public abstract Location getTargetLocation();

    public abstract boolean isDead();
}
