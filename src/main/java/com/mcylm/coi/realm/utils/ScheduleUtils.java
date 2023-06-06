package com.mcylm.coi.realm.utils;

import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduleUtils {

    @Getter
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
}
