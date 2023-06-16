package com.mcylm.coi.realm.enums;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.team.impl.COITeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

/**
 * 解锁类型
 */
@Getter
@AllArgsConstructor
public enum COIHeadType {

    // 上锁的箱子
    LOCK_CHEST(
        "LOCK_CHEST",
        "上锁",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19"
        ),

    // 可用于防御塔
    KILL(
        "KILL",
        "杀戮机器",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZkYjEzN2EzNTY3OWJlYWY3OTAwNzBkMGM5Yzk2YzkwNjc2MjYwZWJjMDBkZDJjNzAwNTYyYTA5OWRiMDdjMCJ9fX0="
        ),

    // 可用于防御塔
    REPAIR(
        "REPAIR",
        "修复机器",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJhMDJhM2JjNzk2MzZlYmNhOTZjYmFkYWY2NWNlNjNhZTcxZDcwYTc3MDg2ODNmODI1MzBhMjZjOWQ1MTczNSJ9fX0="
        ),

    FARMER(
        "FARMER",
        "农夫",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY5YWM5NzZlZjQ4NDQ3Nzc2YmZmZmY3MGRmNjZjZWViNGE1ZGFjNTA1YjEzZjE3MjEyM2UxNGNkMzc1OTE4MSJ9fX0="
        ),

    MINER(
        "MINER",
        "矿工",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdkYTdiYzJlN2Q0YjNkYjhiMzg4MTg3YWY2ODNmNTZhMTIxYzk2MWQ3ODdjNmY4NTFiYmI5Njc0YzkzNzQ2YiJ9fX0="
        ),

    SOLDIER(
        "SOLDIER",
        "战士",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmZjZjE0Mzk5NjIxMThlMWJkODdhYzM0YWMwZmRlZGFmYTIwZTZiOWU0N2MyYTEyNmVhY2FmOTgyNjdlNmIyNSJ9fX0="
        ),

    TEST(
        "TEST",
        "TEST",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE5NDZiMjc2OWJkMjM1ODQwZWUzZjgxOTljY2VkZjA4ZmZjNTc5ZDM2MmY2MDMwZTMwNmMzOTM1YzE1ZTVmN2YifX19"
        ),



    ;


    // CODE
    private String code;
    // 未解锁时显示的名称 name
    private String name;
    // GUI显示的材质
    private String textures;


}
