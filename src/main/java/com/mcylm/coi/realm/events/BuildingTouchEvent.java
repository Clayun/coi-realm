package com.mcylm.coi.realm.events;

import com.mcylm.coi.realm.tools.building.COIBuilding;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Data
@AllArgsConstructor
public class BuildingTouchEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    // 被点击的建筑
    private COIBuilding building;

    // 点击的人
    private Player player;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
