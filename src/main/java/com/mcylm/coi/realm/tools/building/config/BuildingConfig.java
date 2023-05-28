package com.mcylm.coi.realm.tools.building.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class BuildingConfig {
    @SerializedName("max_level")
    private int maxLevel;

    private int consume;

    private Map<Integer, String> structures;
}
