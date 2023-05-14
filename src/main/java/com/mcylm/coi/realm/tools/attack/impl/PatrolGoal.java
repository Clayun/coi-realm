package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.AttackGoal;
import com.mcylm.coi.realm.tools.attack.Commandable;
import lombok.AllArgsConstructor;
import org.bukkit.Location;

import java.util.Random;


public class PatrolGoal extends SimpleGoal {

    int moveAroundTick = 0;

    public PatrolGoal(Commandable npc) {
        super(npc);
    }


    @Override
    public void tick() {

        Random random = new Random();
        Commandable npc = getExecutor();
        if (moveAroundTick++ > 60 + random.nextInt(-10, 60)) {
            moveAroundTick = 0;
            if (npc.getTarget() == null && npc.getLocation() != null) {
                Location to = npc.getLocation().clone().add(random.nextDouble(-16, 16), 0, random.nextDouble(-16, 16));
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
