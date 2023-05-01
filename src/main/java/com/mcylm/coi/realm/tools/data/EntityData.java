package com.mcylm.coi.realm.tools.data;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class EntityData implements MetadataValue {

    @Nullable
    public static COINpc getNpcByEntity(Entity entity) {
        for (MetadataValue value : entity.getMetadata("entityData")) {
            if (value.asString().equals("ENTITY_DATA")) {
                return ((EntityData) value).getNpc();
            }
        }
        return null;
    }

    private COINpc npc;

    @Override
    public @Nullable Object value() {
        return null;
    }

    @Override
    public int asInt() {
        return 0;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public double asDouble() {
        return 0;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public short asShort() {
        return 0;
    }

    @Override
    public byte asByte() {
        return 0;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public @NotNull String asString() {
        return "ENTITY_DATA";
    }

    @Override
    public @Nullable Plugin getOwningPlugin() {
        return Entry.getInstance();
    }

    @Override
    public void invalidate() {

    }
}
