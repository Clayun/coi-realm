package com.mcylm.coi.realm.runnable.api;

public interface GameTaskApi {

    // 启动中
    void waiting();

    // 游戏中
    void gaming();

    // 结算中
    void stopping();

}
