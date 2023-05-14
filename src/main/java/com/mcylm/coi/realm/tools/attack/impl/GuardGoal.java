package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;
import org.bukkit.Location;

public class GuardGoal extends SimpleGoal {

    private Location point;
    private int maxRadius = 20;

    public GuardGoal(Commandable npc, Location point) {
        super(npc);
        this.point = point;
    }


    @Override
    public void tick() {
        Commandable npc = getExecutor();
        if (npc.getLocation() == null) return;
        if (npc.getTarget() == null && npc.getLocation().distance(point) > 8) {
            npc.findPath(point);
        }

        if (npc.getTarget() != null && npc.getLocation().distance(npc.getTarget().getTargetLocation()) > maxRadius) {
            npc.setTarget(null);
            npc.findPath(point);
        }
        getExecutor().lookForEnemy(maxRadius);

    }

    @Override
    public void asyncTick() {

    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.GUARD;
    }
}
