package com.mcylm.coi.realm.tools.building.config;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class BuildingConfig {

    public BuildingConfig() {
        showInMenu = true;
        this.maxLevel = 1;
        this.maxBuild = 1;
        this.consume = 16;
        this.structures = Map.of();
        this.customOptions = new JsonObject();
    }

    @SerializedName("show_in_menu")
    boolean showInMenu;

    @SerializedName("max_level")
    private int maxLevel;
    @SerializedName("max_build")
    private int maxBuild;
    private int consume;

    private Map<Integer, String> structures;

    @SerializedName("custom_options")
    private JsonObject customOptions;
}
