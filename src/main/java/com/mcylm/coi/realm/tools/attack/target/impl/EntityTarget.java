package com.mcylm.coi.realm.tools.attack.target.impl;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.attack.target.TargetType;
import com.mcylm.coi.realm.tools.data.EntityData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
@Setter
public class EntityTarget extends Target {

    private LivingEntity entity;

    public EntityTarget(LivingEntity livingEntity, int p) {
        super(p);
        this.entity = livingEntity;

    }


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
        COINpc npc = EntityData.getNpcByEntity(entity);

        if (npc != null) {
            if (npc.getNpc().isRemoved()) {
                return true;
            }
            return !npc.getNpc().isAlive();
        }
        return entity.isDead();
    }
}
