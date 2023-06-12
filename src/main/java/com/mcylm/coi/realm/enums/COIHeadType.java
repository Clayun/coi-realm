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
        "上锁的箱子",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19"
        ),

    // 可用于防御塔
    ENERGY_CORE(
        "ENERGY_CORE",
        "能量核心",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjY0NGIwN2E1YTcyNGQ2YzM5YTcwZTNjY2RiMmRiMTA3NjgyYTEyZTkwMDViYzIwYjk0MTVlNDA1MDhiYjUxMiJ9fX0="
        ),

    // 可用于防御塔
    ENERGY_CORE2(
        "ENERGY_CORE2",
        "能量核心2",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc0MDBlYTE5ZGJkODRmNzVjMzlhZDY4MjNhYzRlZjc4NmYzOWY0OGZjNmY4NDYwMjM2NmFjMjliODM3NDIyIn19fQ=="
        ),

    FARMER(
        "FARMER",
        "农夫",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E4YWIyMjY3YWU0NDNlODNiMDVlM2E4ZDY3YjhiNzYwYWRmYWFkMzM1YzkwNDczMzhjMGUxNzc0YTY1YzM3MiJ9fX0="
        ),

    MINER(
        "MINER",
        "农夫",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQwMmM2MjVkYzA0MWExYTQxOGFhNmU1MTQ3MGMyMDNmMDMwZmZkNjMxYTQ3YWFlNDAxNTliMDg5YzkyNmQ1NSJ9fX0="
        ),

    SOLDIER(
        "SOLDIER",
        "战士",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmZjZjE0Mzk5NjIxMThlMWJkODdhYzM0YWMwZmRlZGFmYTIwZTZiOWU0N2MyYTEyNmVhY2FmOTgyNjdlNmIyNSJ9fX0="
        ),



    ;


    // CODE
    private String code;
    // 未解锁时显示的名称 name
    private String name;
    // GUI显示的材质
    private String textures;


}
