package com.mcylm.coi.realm.tools.attack.target;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface Target {

    TargetType getType();

    @NotNull Location getTargetLocation();

    boolean isDead();
}
