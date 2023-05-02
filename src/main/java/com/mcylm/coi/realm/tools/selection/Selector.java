package com.mcylm.coi.realm.tools.selection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Selector {

    Map<Player, Selector> selectors = new ConcurrentHashMap<>();

    boolean isStop();

    void stop(boolean sendMsg);

    void selectLocation(Location loc);
}
