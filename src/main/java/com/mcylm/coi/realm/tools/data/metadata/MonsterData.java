package com.mcylm.coi.realm.tools.data.metadata;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.target.Target;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Getter
@Setter
public class MonsterData implements MetadataValue {

    private Target target;


    @Nullable
    public static MonsterData getDataByEntity(Entity entity) {
        for (MetadataValue value : entity.getMetadata("monsterData")) {
            if (value.asString().equals("MONSTER_DATA")) {
                return (MonsterData) value;
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
        return "MONSTER_DATA";
    }

    @Override
    public @Nullable Plugin getOwningPlugin() {
        return Entry.getInstance();
    }

    @Override
    public void invalidate() {

    }
}
