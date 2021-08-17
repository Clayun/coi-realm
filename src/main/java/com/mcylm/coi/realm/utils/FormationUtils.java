package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.model.COIPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 战士NPC的位置编排
 */
public class FormationUtils {

    // 每个NPC之间的间隔
    private static double NPC_SPACE = 1.5;

    /**
     * 自定义阵型编排
     * 建议每秒计算一次，时间间隔越小，阵容还原度越高
     * @param location 玩家当前位置
     * @param customizedFormat 0：空气 1：NPC 2：玩家
     *                         二维数组结构，每行9个，共6行（大型箱子结构）
     * @return
     */
    public static List<Location> calculateFormation(Location location, List<List<Integer>> customizedFormat){

        if(customizedFormat == null){
            return new ArrayList<>();
        }

        // 编排后的阵容
        List<Location> formation = new ArrayList<>();

        COIPosition playerPosition = getPlayerPosition(customizedFormat);

        // 如果没获取到玩家所在位置，就返回空，代表无阵型
        if(playerPosition == null){
            return new ArrayList<>();
        }

        // NPC在第几行
        int NPCRow= 0;
        for(List<Integer> row : customizedFormat){
            NPCRow ++;

            // NPC在第几列
            int NPCColumn = 0;

            for(Integer column : row){
                NPCColumn ++;

                // 找到NPC所在位置
                if(column == 1){
                    int relativeXPosition = NPCRow - playerPosition.getRow();
                    int relativeZPosition = NPCColumn - playerPosition.getColumn();

                    double npcX = location.getX() + (relativeXPosition * NPC_SPACE);
                    double npcZ = location.getZ() + (relativeZPosition * NPC_SPACE);


                    Location npcLocation = new Location(location.getWorld(),npcX,location.getY(),npcZ);

                    formation.add(npcLocation);

                }
            }
        }

        return formation;

    }

    /**
     * 获取玩家在阵型中的相对位置
     * @param customizedFormat
     * @return
     */
    private static COIPosition getPlayerPosition(List<List<Integer>> customizedFormat){
        if(customizedFormat != null
                && !customizedFormat.isEmpty()){

            // 玩家在第几行
            int playerRow= 0;
            for(List<Integer> row : customizedFormat){
                playerRow ++;

                // 玩家在第几列
                int playerColumn = 0;

                for(Integer column : row){
                    playerColumn ++;

                    // 找到玩家所在位置
                    if(column == 2){
                        COIPosition position = new COIPosition();
                        position.setRow(playerRow);
                        position.setColumn(playerColumn);

//                        LoggerUtils.debug("玩家在第"+playerRow+"行，第"+playerColumn+"列");

                        return position;

                    }
                }
            }

        }

        return null;
    }

    /**
     * 测试16人阵营
     * @return
     */
    public static List<List<Integer>> customFormat(){

        List<List<Integer>> result = new ArrayList<>();

        List<Integer> row1 = new ArrayList<>(){{
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
        }};

        List<Integer> row2 = new ArrayList<>(){{
            add(0);
            add(0);
            add(1);
            add(1);
            add(1);
            add(1);
            add(1);
            add(0);
            add(0);
        }};

        List<Integer> row3 = new ArrayList<>(){{
            add(0);
            add(1);
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
            add(1);
            add(0);
        }};

        List<Integer> row4 = new ArrayList<>(){{
            add(0);
            add(1);
            add(0);
            add(0);
            add(2);
            add(0);
            add(0);
            add(1);
            add(0);
        }};

        List<Integer> row5 = new ArrayList<>(){{
            add(0);
            add(1);
            add(0);
            add(0);
            add(0);
            add(0);
            add(0);
            add(1);
            add(0);
        }};

        List<Integer> row6 = new ArrayList<>(){{
            add(0);
            add(0);
            add(1);
            add(1);
            add(1);
            add(1);
            add(1);
            add(0);
            add(0);
        }};

        result.add(row1);
        result.add(row2);
        result.add(row3);
        result.add(row4);
        result.add(row5);
        result.add(row6);

        return result;
    }

}
