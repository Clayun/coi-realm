package com.mcylm.coi.realm.tools.team.impl;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.enums.COIBuildingType;
import com.mcylm.coi.realm.enums.COIGameStatus;
import com.mcylm.coi.realm.enums.COITeamType;
import com.mcylm.coi.realm.events.GameStatusEvent;
import com.mcylm.coi.realm.tools.building.COIBuilding;
import com.mcylm.coi.realm.utils.LoggerUtils;
import com.mcylm.coi.realm.utils.TeamUtils;
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
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Data
public class COIScoreboard {

    MetadataKey<ScoreboardObjective> SCOREBOARD_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);


    /**
     * 获取每个小队该显示的Scoreboard
     */
    public void showBoard(){

        BiConsumer<Player, ScoreboardObjective> updater = (p, obj) -> {

            COITeam team = TeamUtils.getTeamByPlayer(p);

            int baseLevel = 0;

            for (COIBuilding finishedBuilding : team.getFinishedBuildings()) {

                if(finishedBuilding.getType().equals(COIBuildingType.BASE)){
                    baseLevel = finishedBuilding.getLevel();
                }

            }

            List<String> str = new ArrayList<>();

            str.add("&f<&aLV."+baseLevel+"&f> "+team.getType().getColor()+team.getType().getName());
            str.add("&e团队战分&7(奖励) &f"+team.getScore());
            str.add("&b♚ 团队资源 &7资产");
            str.add("&a● &a绿宝石 &f"+team.getPublicEmerald());
            str.add("&e● &a建筑数量 &f"+team.getFinishedBuildings().size());
            str.add("&d● &a总人口 &f"+team.getTotalPeople());
            str.add("&b♚ 战局状态 &7基地/积分");
            str.add(" ");

            List<COITeam> teams = Entry.getGame().getTeams();

            for(COITeam coiTeam : teams){

                if(coiTeam.getType().equals(COITeamType.MONSTER)){
                    continue;
                }

                COIBuilding base = coiTeam.getBase();
                if(base != null){
                    str.add("&a● "+coiTeam.getType().getColor() + coiTeam.getType().getName()+" &f"+base.getHealth()+"&e/"+coiTeam.getScore());
                }else{
                    str.add("&7● "+coiTeam.getType().getColor() + coiTeam.getType().getName()+" &7&m已被摧毁");
                }
            }

            str.add("");


            // TODO 标题自动闪动颜色
            obj.setDisplayName("&6&l岛屿冲突");
            obj.applyLines(str);
        };

        Scoreboard sb = Services.load(ScoreboardProvider.class).getScoreboard();

        // 游戏状态变更事件
        Events.subscribe(GameStatusEvent.class)
                .handler(e -> {
                    // 游戏状态变更为游戏中的时候，给全体玩家上一个scoreboard
                    if(e.getStatus().equals(COIGameStatus.GAMING)){

                        for(Player p : Bukkit.getOnlinePlayers()){
                            ScoreboardObjective obj = sb.createPlayerObjective(p, "null", DisplaySlot.SIDEBAR);
                            Metadata.provideForPlayer(p).put(SCOREBOARD_KEY, obj);

                            updater.accept(p, obj);
                        }
                    }

                });

        // 进入事件
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    // 玩家进入游戏的时候，上计分板
                    if(Entry.getGame().getStatus().equals(COIGameStatus.GAMING)){

                        Player p = e.getPlayer();
                        ScoreboardObjective obj = sb.createPlayerObjective(p, "null", DisplaySlot.SIDEBAR);
                        Metadata.provideForPlayer(p).put(SCOREBOARD_KEY, obj);

                        updater.accept(p, obj);
                    }

                });

        Schedulers.sync().runRepeating(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                MetadataMap metadata = Metadata.provideForPlayer(player);
                ScoreboardObjective obj = metadata.getOrNull(SCOREBOARD_KEY);
                if (obj != null) {
                    updater.accept(player, obj);
                }
            }
        }, 3L, 3L);
    }



}
