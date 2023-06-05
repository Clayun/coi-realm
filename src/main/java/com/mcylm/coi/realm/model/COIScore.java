package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.enums.COIScoreType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
public class COIScore {

    // 积分变化类型
    private COIScoreType type;

    // 时间
    private LocalDateTime time;

    // 玩家
    private Player player;

}
