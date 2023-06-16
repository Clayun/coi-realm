package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.model.COINpc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class COIMinerCreator extends COINpc {

    // 必须要有装东西的箱子
    public COIMinerCreator(List<Location> chestsLocation) {
        super();
        this.chestsLocation = chestsLocation;
    }

    // 收集够这些资源就回去，默认64个
    private Integer resourceLimitToBack = 64;

    // 收集的物资存放箱子的位置，可以有多个
    private List<Location> chestsLocation;

}
