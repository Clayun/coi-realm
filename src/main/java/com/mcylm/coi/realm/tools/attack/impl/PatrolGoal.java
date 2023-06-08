package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;
import org.bukkit.Location;

import java.util.Random;



public class PatrolGoal extends SimpleGoal {

    int moveAroundTick = 0;

    // 最大巡逻范围
    int maxRadius = 20;
    // 巡逻中心位置
    Location point;
    public PatrolGoal(Commandable npc) {
        super(npc);
        point = npc.getLocation();
    }

    public PatrolGoal(Commandable npc, int maxRadius, Location point) {
        super(npc);
        this.maxRadius = maxRadius;
        this.point = point;
    }


    @Override
    public void tick() {


        Random random = new Random();
        Commandable npc = getExecutor();
        if (npc.getLocation() == null) return;
        if (npc.getLocation().distance(point) > maxRadius) {
            npc.findPath(point);
            npc.setTarget(null);
        }
        if (moveAroundTick++ > 60 + random.nextInt(-10, 60)) {
            moveAroundTick = 0;
            if (npc.getTarget() == null) {
                Location to = npc.getLocation().clone().add(random.nextDouble(-16, 16), 0, random.nextDouble(-16, 16));
                if (to.distance(point) > maxRadius) {
                    return;
                }
                Location finalTo = to.getWorld().getHighestBlockAt(to).getLocation();
                npc.findPath(finalTo);
            }
        }

        getExecutor().lookForEnemy(-1);

    }

    @Override
    public void asyncTick() {

    }


    @Override
    public AttackGoalType getType() {
        return AttackGoalType.PATROL;
    }
}
