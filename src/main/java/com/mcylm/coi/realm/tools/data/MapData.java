package com.mcylm.coi.realm.tools.data;

import com.mcylm.coi.realm.tools.map.COIMobSpawnPoint;
import com.mcylm.coi.realm.tools.map.COIVein;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏地图数据
 */

@Setter
@Getter
@NoArgsConstructor
public class MapData {

    // 矿脉的位置
    private List<COIVein> veins = new ArrayList<>();

    private List<COIMobSpawnPoint> mobSpawnPoints = new ArrayList<>(;)
}
