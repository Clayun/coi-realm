package com.mcylm.coi.realm.tools.attack.impl;

import com.mcylm.coi.realm.enums.AttackGoalType;
import com.mcylm.coi.realm.tools.attack.Commandable;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import org.bukkit.entity.LivingEntity;

public class TeamFollowGoal extends SimpleGoal {

    private AttackTeam team;
    private LivingEntity followingEntity;
    public TeamFollowGoal(Commandable npc, AttackTeam team) {
        super(npc);
        this.team = team;
    }

    @Override
    public void tick() {
        Commandable npc = getExecutor();

        int index = team.getMembers().indexOf((COIEntity) npc);

        if (npc.getTarget() == null && followingEntity != null) {
            if (followingEntity.isDead() && index > 0) {
                team.getMembers().remove(index - 1);
            }
            if (npc.getLocation() != null && npc.getLocation().distance(npc.getCommander().getLocation()) >= 3) {
                npc.findPath(followingEntity.getLocation());
            }
        }

        getExecutor().lookForEnemy(-1);
    }

    @Override
    public void asyncTick() {

        COIEntity entity = (COIEntity) getExecutor();

        int index = team.getMembers().indexOf(entity);
        if (index == 0) {
            followingEntity = team.getCommander();
        } else {
            followingEntity = (LivingEntity) team.getMembers().get(index - 1).getNpc().getEntity();
        }



    }

    @Override
    public AttackGoalType getType() {
        return AttackGoalType.TEAM_FOLLOW;
    }


}
