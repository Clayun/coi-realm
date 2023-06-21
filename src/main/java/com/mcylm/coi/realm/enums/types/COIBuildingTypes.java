package com.mcylm.coi.realm.enums.types;

import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIUnlockType;

import java.util.HashSet;
import java.util.Set;

public class COIBuildingTypes {

    private static final Set<COIBuildingType> values = new HashSet<>();

    public static Set<COIBuildingType> values() {
        return values;
    }
}
