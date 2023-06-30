package com.mcylm.coi.realm.tools.team.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.events.GameStatusEvent;
import com.mcylm.coi.realm.game.COIGame;
import com.mcylm.coi.realm.model.COINpc;
import com.mcylm.coi.realm.tools.attack.target.impl.EntityTarget;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.tools.data.metadata.EntityData;
import com.mcylm.coi.realm.tools.npc.COISoldierCreator;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import com.mcylm.coi.realm.utils.DamageUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Data;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.Services;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.metadata.MetadataMap;
import me.lucko.helper.scoreboard.Scoreboard;
import me.lucko.helper.scoreboard.ScoreboardObjective;
import me.lucko.helper.scoreboard.ScoreboardProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

@Data
public class COIScoreboard {

    private HashMap<String,FastBoard> playerScoreboard = new HashMap<>();

    public void showBoard() {


        BiConsumer<Player, FastBoard> updater = (p, board) -> {

            COITeam team = TeamUtils.getTeamByPlayer(p);

            if(team != null){
                int baseLevel = 0;

                for (COIBuilding finishedBuilding : team.getFinishedBuildings()) {

                    if(finishedBuilding.getType().equals(COIBuildingType.BASE)){
                        baseLevel = finishedBuilding.getLevel();
                    }

                }


                List<String> str = new ArrayList<>();

                Double playerScore = Entry.getGame().getPlayerScore(p);

                str.add(LoggerUtils.replaceColor("&f<&aLV."+baseLevel+"&f> "+team.getType().getColor()+team.getType().getName()));
                str.add(LoggerUtils.replaceColor("&e本局战分&7(奖励) &f"+playerScore));
                str.add(LoggerUtils.replaceColor("&b♚ 团队资源 &7资产"));
                str.add(LoggerUtils.replaceColor("&a● &a绿宝石 &f"+team.getPublicEmerald()));
                str.add(LoggerUtils.replaceColor("&e● &a建筑数量 &f"+team.getFinishedBuildings().size()));
                str.add(LoggerUtils.replaceColor("&d● &a总人口 &f"+team.getTotalPeople()));
                str.add(LoggerUtils.replaceColor("&b♚ 队伍 &7基地血量"));
                str.add(" ");

                List<COITeam> teams = Entry.getGame().getTeams();

                for(COITeam coiTeam : teams){

                    if(coiTeam.getType().equals(COITeamType.MONSTER)){
                        continue;
                    }

                    COIBuilding base = coiTeam.getBase();
                    if(base != null){
                        str.add(LoggerUtils.replaceColor("&a● "+coiTeam.getType().getColor() + coiTeam.getType().getName()+" &f"+base.getHealth()+"&7/"+coiTeam.getBase().getMaxHealth()));
                    }else{
                        str.add(LoggerUtils.replaceColor("&7● "+coiTeam.getType().getColor() + coiTeam.getType().getName()+" &7&m已被摧毁"));
                    }
                }

                str.add("");

                board.updateLines(str);
            }

        };

        Events.subscribe(PlayerJoinEvent.class).handler(e -> {
            Player player = e.getPlayer();
            FastBoard board = new FastBoard(player);
            board.updateTitle(LoggerUtils.replaceColor("&6岛屿冲突"));
            playerScoreboard.put(player.getName(), board);
        });

        Events.subscribe(PlayerQuitEvent .class).handler(e -> {
            Player player = e.getPlayer();
            FastBoard board = playerScoreboard.remove(player.getName());
            if (board != null) {
                board.delete();
            }
        });

        Schedulers.sync().runRepeating(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                FastBoard board = playerScoreboard.get(player.getName());
                updater.accept(player, board);
            }
        }, 3L, 3L);


    }



}
