package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.enums.COIScoreType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
public class COIPlayerScore {

    // 玩家名称
    private String player;

    // 总积分
    private double score;

    // 积分明细
    private List<COIScore> scoreList;

}
