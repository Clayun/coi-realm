package com.mcylm.coi.realm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

/**
 * 封装后的方块
 */
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class COIBlock {

    private Integer x;
    private Integer y;
    private Integer z;

    private String blockData;
    private String material;
    private String world;

    public Block getBlock(){

        if(StringUtils.isBlank(world)){
            return null;
        }

        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

}
