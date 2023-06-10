package com.mcylm.coi.realm.tools.trait;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Entity;

import java.util.Optional;


public class DisguiseTrait extends Trait {

    private Disguise disguise;
    private boolean appliedSkin = false;

    public void setDisguise(Disguise disguise) {
        this.disguise = disguise;
        disguise();
    }
    public DisguiseTrait(){
        super("Disguise");
    }

    public Optional<Disguise> getDisguise() {
        return Optional.ofNullable(this.disguise);
    }

    @Override
    public void onSpawn() {
        disguise();
    }

    private void disguise() {

        if (getNPC() == null || !getNPC().isSpawned()) {
            return;
        }

        // citizens will respawn the NPC after changing its skin
        // not checking this will cause an endless loop
        if (appliedSkin) return;

        getDisguise().ifPresent(disguise -> {
            appliedSkin = true;
            Entity entity = getNPC().getEntity();
            disguise.setEntity(entity);
            disguise.setDynamicName(true);
            disguise.startDisguise();
        });
    }
}