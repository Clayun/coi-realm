package com.mcylm.coi.realm.tools.attack;

import com.mcylm.coi.realm.tools.attack.target.Target;
import com.mcylm.coi.realm.tools.npc.impl.COIEntity;
import org.bukkit.Location;

public interface DamageableAI {


    default boolean setTarget(Target target) {
        if (target == null) {
            setTargetDirectly(null);
            return true;
        }
        if (getTarget() == null || getTarget().isDead()) {
            setTargetDirectly(target);
            return true;
        }
        if (target.getTargetLevel() >= getTarget().getTargetLevel()) {
            setTargetDirectly(target);
            return true;
        }
        return false;
    }

    default COIEntity asEntity() {
        return (COIEntity) this;
    }
    void lookForEnemy(int radius);

    Target getTarget();

    void setTargetDirectly(Target target);

    void damage(Target target, double damage, Location attackLocation);

    Location getLocation();

    void findPath(Location location);
}
