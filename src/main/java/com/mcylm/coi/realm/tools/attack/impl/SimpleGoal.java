package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.attack.Commandable;
import lombok.AllArgsConstructor;
import org.bukkit.Location;

public abstract class SimpleGoal implements AttackGoal {

    private Commandable npc;
    private boolean stop;

    public SimpleGoal(Commandable npc) {
        this.npc = npc;
        this.stop = true;
    }

    @Override
    public void start() {
        stop = false;
    }


    @Override
    public void stop() {

        this.stop = true;
    }

    @Override
    public Commandable getExecutor() {
        return npc;
    }

    @Override
    public boolean isStop() {
        return stop;
    }

}
