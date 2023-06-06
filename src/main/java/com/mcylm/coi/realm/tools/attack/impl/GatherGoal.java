package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;

public class GatherGoal extends SimpleGoal {

    private final boolean force;

    public GatherGoal(Commandable npc, boolean force) {
        super(npc);
        this.force = force;
    }

    @Override
    public void tick() {
        Commandable npc = getExecutor();
        if (npc.getTarget() != null && force) {
            npc.setTarget(null);
        }

        if (npc.getTarget() == null && npc.getCommander() != null) {
            if (npc.getLocation() != null && npc.getLocation().distance(npc.getCommander().getLocation()) >= 10) {
                npc.findPath(npc.getCommander().getLocation());
            }
        }
    }

    @Override
    public void asyncTick() {

    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.GATHER;
    }
}
