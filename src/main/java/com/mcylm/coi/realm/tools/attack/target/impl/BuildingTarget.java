package com.mcylm.coi.realm.tools.attack.target.impl;

import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class BuildingTarget implements Target {

    private COIBuilding building;

    @Override
    public TargetType getType() {
        return TargetType.BUILDING;
    }

    @Override
    public @NotNull Location getTargetLocation() {
        return building.getHologramPoint();
    }

    @Override
    public boolean isDead() {
        return !building.isAlive();
    }
}
