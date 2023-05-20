package com.mcylm.coi.realm.player;

import com.mcylm.coi.realm.player.settings.PlayerSettings;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.target.Target;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class COIPlayer {


    private Player player;
    @Getter
    private Set<Target> selectedTargets = new HashSet<>();

    @Getter
    private Set<Commandable> teamNpcs = new HashSet<>();

    @Getter
    private Set<Commandable>selectedNpcs = new HashSet<>();

    @Setter
    @Getter
    @Nullable
    private Target attackedTarget;

    @Getter
    private PlayerSettings settings = new PlayerSettings();

    public Player getBukkitPlayer() {
        return player;
    }
}
