package com.mcylm.coi.realm.model;

import com.mcylm.coi.realm.enums.COIScoreType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class COIScoreDetail {

    // 积分类型
    private COIScoreType type;

    // 当前类型的总积分
    private double score;

    // 总次数
    private int count;

    @Override
    public String toString() {
        return type.getName()+" x"+count+" （奖励积分："+score+"）";
    }
}
