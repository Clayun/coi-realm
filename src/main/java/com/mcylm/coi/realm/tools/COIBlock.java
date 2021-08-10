package com.mcylm.coi.realm.tools;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * 封装后的方块
 */
@Data
public class COIBlock {

    private Double x;
    private Double y;
    private Double z;

    private String blockData;
    private String material;

}
