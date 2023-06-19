package com.mcylm.coi.realm.tools.data;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.model.COISkin;
import com.mcylm.coi.realm.tools.map.COIVein;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 玩家的建筑/NPC 皮肤类
 */

@Data
@NoArgsConstructor
public class SkinData {

    // 建筑皮肤类
    private List<COISkin> skins = new ArrayList<>();

}
