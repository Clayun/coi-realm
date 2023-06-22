package com.mcylm.coi.realm.tools.goals;

import com.mcylm.coi.realm.tools.attack.DamageableAI;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import org.bukkit.entity.Mob;

public class NPCLookForTargetGoal extends BehaviorGoalAdapter {
    private Mob entity;

    private DamageableAI ai;

    private int tick = 0;
    public NPCLookForTargetGoal(DamageableAI ai) {
        this.ai = ai;
        this.entity = (Mob) ai.asEntity().getNpc().getEntity();

    }
    /*
    @Override
    public boolean shouldActivate() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void start() {
        LoggerUtils.debug("start find");
    }

    @Override
    public void stop() {
        LoggerUtils.debug("stop find");
    }
    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return GoalKey.of(Mob.class, Entry.getNamespacedKey("npc_look_for_target"));
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }


     */

    @Override
    public void reset() {

    }

    @Override
    public BehaviorStatus run() {
        if (tick++ > 4) {
            tick = 0;
            ai.lookForEnemy(-1);
        }
        return BehaviorStatus.RUNNING;


    }

    @Override
    public boolean shouldExecute() {
        if (!ai.asEntity().isAlive()) {
            return false;
        }
        this.entity = (Mob)ai.asEntity().getNpc().getEntity();

        if (ai.asEntity().isTooHungryToWork()) {
            return false;
        }

        if (ai.getTarget() == null) {
            return true;
        }
        return false;
    }
}
