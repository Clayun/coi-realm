package com.mcylm.coi.realm.tools.goals.citizens;

import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.data.metadata.AttackTeamData;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

public class NPCMoveToTargetPointGoal extends BehaviorGoalAdapter {

    private Mob entity;

    private COIEntity coiEntity;

    private AttackTeam team;

    private Location currentTarget;

    boolean reach = true;

    public NPCMoveToTargetPointGoal(COIEntity entity) {
        this.coiEntity = entity;
        this.entity = (Mob) entity.getNpc().getEntity();

    }

    @Override
    public void reset() {

    }

    @Override
    public BehaviorStatus run() {

        if (currentTarget != team.getTarget()) {
            currentTarget = team.getTarget();
            reach = false;
        }

        if (!reach) {
            if (coiEntity instanceof COISoldier soldier) {
                soldier.setTarget(null);
                entity.setTarget(null);
            }
            coiEntity.findPath(currentTarget);
        }

        if (entity.getLocation().distance(currentTarget) < 3.5) {
            reach = true;
        }

        return BehaviorStatus.RUNNING;

    }

    @Override
    public boolean shouldExecute() {
        if (!coiEntity.isAlive()) {
            return false;
        }
        this.entity = (Mob) coiEntity.getNpc().getEntity();


        AttackTeamData teamData = AttackTeamData.getDataByEntity(entity);

        if (teamData == null) {
            return false;
        }

        team = teamData.getCurrentTeam();

        if (team.getCommander().isDead()) {
            return false;
        }

        if (coiEntity.isTooHungryToWork()) {
            return false;
        }

        if (reach && currentTarget != null && currentTarget == team.getTarget()) {
            return false;
        }

        return true;
    }
}
