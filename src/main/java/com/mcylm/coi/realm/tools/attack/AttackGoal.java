package com.mcylm.coi.realm.tools.attack;

import com.destroystokyo.paper.entity.ai.GoalType;
import com.mcylm.coi.realm.enums.AttackGoalType;

public interface AttackGoal {

    void start();

    void tick();

    void asyncTick();

    void stop();

    Commandable getExecutor();
    boolean isStop();

    AttackGoalType getType();
}
