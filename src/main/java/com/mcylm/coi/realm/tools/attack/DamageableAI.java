package com.mcylm.coi.realm.tools.attack;

import com.mcylm.coi.realm.tools.attack.target.Target;
import org.bukkit.Location;

public interface DamageableAI {


    default boolean setTarget(Target target) {
        if (target == null) {
            setTargetDirectly(null);
            return true;
        }
        if (getTarget() == null) {
            setTargetDirectly(target);
            return true;
        }
        if (target.getTargetLevel() >= getTarget().getTargetLevel()) {
            setTargetDirectly(target);
            return true;
        }
        return false;
    };

    void lookForEnemy(int radius);

    Target getTarget();

    void setTargetDirectly(Target target);

    void damage(Target target, double damage, Location attackLocation);

    Location getLocation();

    void findPath(Location location);
}
