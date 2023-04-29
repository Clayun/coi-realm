package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.model.COIStructure;
import lombok.*;
import org.bukkit.Location;

@Getter @Setter @RequiredArgsConstructor @ToString
public class ClipboardLocation {

    //选取的第一个点
    private Location firstPoint;
    //选取的第二个点
    private Location secondPoint;
    //COI结构体
    private COIStructure structure;

}
