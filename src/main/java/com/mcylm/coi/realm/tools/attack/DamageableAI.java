package com.mcylm.coi.realm.tools.attack;

import com.mcylm.coi.realm.tools.attack.target.Target;
import org.bukkit.Location;

public interface DamageableAI {


    void onTarget(Target target);

    void damage(Target target, double damage, Location attackLocation);

}
