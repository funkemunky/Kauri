package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.api.checks.CheckManager;
import cc.funkemunky.anticheat.api.data.banwave.BanwaveManager;
import cc.funkemunky.anticheat.api.data.logging.LoggerManager;
import cc.funkemunky.anticheat.api.data.stats.StatsManager;
import cc.funkemunky.anticheat.api.pup.AntiPUPManager;

public class KauriAPI {

    public static KauriAPI INSTANCE;

    public KauriAPI() {
        INSTANCE = this;
    }

    public CheckManager getCheckManager() {
        return Kauri.getInstance().getCheckManager();
    }

    public BanwaveManager getBanwaveManager() {
        return Kauri.getInstance().getBanwaveManager();
    }

    public AntiPUPManager getAntiPUPManager() {
        return Kauri.getInstance().getAntiPUPManager();
    }

    public LoggerManager getLoggerManager() {
        return Kauri.getInstance().getLoggerManager();
    }

    public StatsManager getStatsManager() {
        return Kauri.getInstance().getStatsManager();
    }
}
