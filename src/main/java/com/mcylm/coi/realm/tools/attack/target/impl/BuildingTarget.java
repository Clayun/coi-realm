package com.mcylm.coi.realm.tools.attack.target.impl;

import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter

public class BuildingTarget extends Target {

    private COIBuilding building;
    private Location location;

    public BuildingTarget(COIBuilding building, Location location, int p) {
        super(p);
        this.building = building;
        this.location = location;
    }

    @Override
    public TargetType getType() {
        return TargetType.BUILDING;
    }

    @Override
    public @NotNull Location getTargetLocation() {
        return location;
    }

    @Override
    public boolean isDead() {
        return !building.isAlive();
    }
}
