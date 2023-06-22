package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.model.COINpc;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class COISoldierCreator extends COINpc {

    @Override
    public double getAlertRadius() {
        return 20;
    }

    public COISoldierCreator(){

    }
}
