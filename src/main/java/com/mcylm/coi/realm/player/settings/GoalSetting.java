package com.mcylm.coi.realm.player.settings;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventPriority;

@Getter
@Setter
public class GoalSetting {
    private int priority = 5;
    private boolean forceGather = false;

}
