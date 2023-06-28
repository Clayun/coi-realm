package com.mcylm.coi.realm.tools.handler;

import com.mcylm.coi.realm.model.COIBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockPlaceCondition {
    boolean check(Block block, COIBlock blockToPlace, Material material);
}
