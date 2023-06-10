package com.mcylm.coi.realm.events;

import com.mcylm.coi.realm.enums.COIGameStatus;
import lombok.Data;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Data
public class GameStatusEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    // 最新的游戏状态
    private COIGameStatus status;

    public GameStatusEvent(COIGameStatus status) {
        this.status = status;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
