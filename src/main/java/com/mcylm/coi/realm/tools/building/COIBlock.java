package com.mcylm.coi.realm.tools.building;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * 封装后的方块
 */
@Data
public class COIBlock {

    private Integer x;
    private Integer y;
    private Integer z;

    private String blockData;
    private String material;

}
