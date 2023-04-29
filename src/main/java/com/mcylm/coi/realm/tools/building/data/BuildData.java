package com.mcylm.coi.realm.tools.building.data;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class BuildData implements MetadataValue {

    @Nullable
    public static COIBuilding getBuildingByBlock(Block block) {
        for (MetadataValue value : block.getMetadata("building")) {
            if (value.asString().equals("BUILDING_DATA")) {
                return ((BuildData) value).getBuilding();
            }
        }
        return null;
    }

    private COIBuilding building;

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
        return "BUILDING_DATA";
    }

    @Override
    public @Nullable Plugin getOwningPlugin() {
        return Entry.getInstance();
    }

    @Override
    public void invalidate() {

    }
}
