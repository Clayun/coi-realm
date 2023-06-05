package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;

public class FollowGoal extends SimpleGoal {

    public FollowGoal(Commandable npc) {
        super(npc);
    }

    // 跟随范围，超出范围会跟上玩家
    private int maxRadius = 5;

    @Override
    public void tick() {
        Commandable npc = getExecutor();

        if (npc.getTarget() == null && npc.getCommander() != null) {
            if (npc.getLocation() != null && npc.getLocation().distance(npc.getCommander().getBukkitPlayer().getLocation()) >= maxRadius) {
                npc.findPath(npc.getCommander().getBukkitPlayer().getLocation());
            }
        }

        getExecutor().lookForEnemy(maxRadius);
    }

    @Override
    public void asyncTick() {

    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.FOLLOW;
    }
}
