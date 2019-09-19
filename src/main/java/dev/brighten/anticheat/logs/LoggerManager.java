package dev.brighten.anticheat.logs;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.carbon.db.Database;

public class LoggerManager {

    public Database logsDatabase;

    public LoggerManager() {

    }

    public LoggerManager(boolean aLittleStupid) {
        if(aLittleStupid) {
            Atlas.getInstance().getCarbon().createFlatfileDatabase("logs");
            logsDatabase = Atlas.getInstance().getCarbon().getDatabase("logs");
        }
    }
}