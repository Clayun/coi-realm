package com.mcylm.coi.realm.tools.npc;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.npc.impl.COIHuman;
import com.mcylm.coi.realm.tools.npc.impl.COISoldier;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * AI军队执行器
 */
public class COIRunner {

    // 执行器是否已启动（可以设置FALSE来关闭运行）
    boolean isRunning = false;

    // 当前执行器内的NPC集合
    private List<COIHuman> npcList;

    public COIRunner(List<COIHuman> npcList) {
        this.npcList = npcList;

        if(npcList == null){
            npcList = new ArrayList<>();
        }
    }

    /**
     * 在执行器里面添加NPC
     * @param coiHuman
     */
    public void addNpc(COIHuman coiHuman){
        npcList.add(coiHuman);
        if(getNpcList().size() == 1){
            run();
        }
    }

    /**
     * 设置阵型
     * @param formation
     */
    public void updateFormation(List<List<Integer>> formation){

        int i = 0;
        for(COIHuman human : getNpcList()){
            if(human instanceof COISoldier){
                i++;
                COISoldier soldier = (COISoldier) human;
                soldier.setNumber(i);
                soldier.updateFormats(formation);
            }
        }
    }


    /**
     * 启动NPC
     */
    public void run(){

        // 如果已经在运行了，就不再重复执行
        if(isRunning){
            return;
        }

        // 设置启动
        isRunning = true;

        new BukkitRunnable(){


            @Override
            public void run() {

                if(!isRunning){
                    this.cancel();
                }

                List<COIHuman> npcList = getNpcList();

                if(!npcList.isEmpty()){
                    for(COIHuman coiHuman : npcList){

                        if(coiHuman.isAlive()){

                            // 执行NPC移动方法
                            coiHuman.move();
                        }
                    }
                }



            }

        }.runTaskTimer(Entry.getInstance(),0,20L);
    }

    /**
     * 停止运行
     */
    public void stop(){
        isRunning = false;
    }

    public List<COIHuman> getNpcList() {
        return npcList;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
