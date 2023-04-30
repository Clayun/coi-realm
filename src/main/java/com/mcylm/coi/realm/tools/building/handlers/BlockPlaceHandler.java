package com.mcylm.coi.realm.tools.building.handlers;

import com.mcylm.coi.realm.model.COIBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockPlaceHandler {

    Material handle(Block block, COIBlock blockToPlace, Material material);
}
