package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.model.COINpc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class COICartCreator extends COINpc {

    // 搬运工
    public COICartCreator() {
        super();
    }

    // 收集够这些资源就回去，默认5个
    private Integer resourceLimitToBack = 5;

}
