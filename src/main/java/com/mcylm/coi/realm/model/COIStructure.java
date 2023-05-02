package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.rotation.Rotation;
import com.mcylm.coi.realm.utils.rotation.Vector2Int;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑结构
 */
@Getter @Setter @RequiredArgsConstructor @ToString

public class COIStructure implements Cloneable {

    private String name;
    private String fileName;
    private Integer length;
    private Integer width;
    private Integer height;
    private List<COIBlock> blocks;

    @Override
    public COIStructure clone() {
        try {
            COIStructure cloned = (COIStructure) super.clone();
            List<COIBlock> clonedBlocks = new ArrayList<>();

            for (COIBlock block : this.blocks) {
                clonedBlocks.add(block.clone()); // ?? COIBlock ????????? clone ??
            }

            cloned.blocks = clonedBlocks;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning error: " + e.getMessage());
        }
    }

    public void rotate(Rotation rotation) {


        for (COIBlock block : blocks) {
            int x = block.getX();
            int z = block.getZ();

            Vector2Int r = Vector2Int.of(x, z).rotate(rotation);
            int rX = r.getX();
            int rZ = r.getZ();

            block.setX(rX);
            block.setZ(rZ);
            BlockData data = Bukkit.createBlockData(block.getBlockData());
            int rv = Math.round(rotation.getDegrees() / 90f);

            if (data instanceof Rotatable rotatable) {
                rotatable.setRotation(LocationUtils.rotateBlockFace(rotatable.getRotation(), rv, false));
            }

            if (data instanceof Directional directional) {
                directional.setFacing(LocationUtils.rotateBlockFace(directional.getFacing(), rv, false));
            }
            block.setBlockData(data.getAsString());
        }
    }

}
