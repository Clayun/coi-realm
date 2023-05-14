package com.mcylm.coi.realm.player;

import com.mcylm.coi.realm.player.settings.PlayerSettings;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.target.Target;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class COIPlayer {


    private Player player;
    private Set<Target> selectedTargets = new HashSet<>();

    private Set<Commandable> teamNpcs = new HashSet<>();

    private Set<Commandable>selectedNpcs = new HashSet<>();

    @Nullable
    private Target attackedTarget;

    private PlayerSettings settings = new PlayerSettings();

}
