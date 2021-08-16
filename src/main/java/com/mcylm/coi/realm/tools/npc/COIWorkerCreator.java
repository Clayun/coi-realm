package com.mcylm.coi.realm.tools.npc;

import lombok.Data;
import org.bukkit.Location;

@Data
public class COIWorkerCreator extends COINpc{

    // 必须要有装东西的箱子
    public COIWorkerCreator(Location chestLocation) {
        super();
        this.chestLocation = chestLocation;
    }

    // 收集够这些资源就回去
    private Integer resourceLimitToBack = 5;

    // 箱子位置
    private Location chestLocation;

}
