package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.team.AttackTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class COISoldierCreator extends COINpc {


    private AttackTeam attackTeam;

    @Override
    public double getAlertRadius() {
        return 20;
    }

    public COISoldierCreator(){

    }
}
