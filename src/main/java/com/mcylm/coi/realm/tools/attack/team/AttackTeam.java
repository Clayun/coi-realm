package com.mcylm.coi.realm.tools.attack.team;


import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

// 怪物的战斗小队
@Getter
@Setter
@Accessors(chain = true)
public class AttackTeam {
    // 队长
    private LivingEntity commander;

    // 队员
    List<COIEntity> members = new ArrayList<>();
}
