package com.mcylm.coi.realm.player.settings;

import com.mcylm.coi.realm.enums.AttackGoalType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PlayerSettings {
    private Map<AttackGoalType, GoalSetting> goalSettings = new HashMap<>();
}
