package com.mcylm.coi.realm.tools.data.metadata;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AttackTeamData implements MetadataValue {


    private AttackTeam currentTeam;

    private Status status = Status.FOLLOWING;

    public AttackTeamData(AttackTeam attackTeam) {
        this.currentTeam = attackTeam;
    }

    @Nullable
    public static AttackTeamData getDataByEntity(Entity entity) {
        for (MetadataValue value : entity.getMetadata("teamData")) {
            if (value.asString().equals("TEAM_DATA")) {
                return (AttackTeamData) value;
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
        return "TEAM_DATA";
    }

    @Override
    public @Nullable Plugin getOwningPlugin() {
        return Entry.getInstance();
    }

    @Override
    public void invalidate() {

    }

    public enum Status {
        FOLLOWING,
        OTHER
    }
}
