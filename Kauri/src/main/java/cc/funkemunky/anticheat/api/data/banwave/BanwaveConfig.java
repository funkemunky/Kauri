package cc.funkemunky.anticheat.api.data.banwave;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import lombok.Getter;

@Init
public class BanwaveConfig {
    @Getter
    private static BanwaveConfig instance;

    public BanwaveConfig() {
        instance = this;
    }

    @ConfigSetting(path = "banwave", name = "enabled")
    public boolean enabled = false;

    @ConfigSetting(path = "banwave", name = "broadcast")
    public boolean broadcast = false;

    @ConfigSetting(path = "banwave", name = "messageAdmins")
    public boolean msgAdmins = true;

    @ConfigSetting(path = "banwave", name = "banInstantly")
    public boolean banInstantly = true;

    @ConfigSetting(path = "banwave", name = "punishCommand")
    public String punishCommand = "ban %player% [Kauri] Judgement Day";

    @ConfigSetting(path = "banwave.interval", name = "intervalTime")
    public int intervalTime = 30;

    @ConfigSetting(path = "banwave.interval", name = "timeUnit")
    public String intervalUnit = "minutes";

    @ConfigSetting(path = "banwave.banRate", name = "banSeconds")
    public int banSeconds = 2;

    @ConfigSetting(path = "banwave.message", name = "start")
    public String startBanwave = "&8[&6&lKauri&8] &7Now running a banwave, where everyone will be judged as either a cheater, or legitimate.";

    @ConfigSetting(path = "banwave.message", name = "foundCheater")
    public String foundCheater = "&8[&6&lKauri&8] &7Judged &e%player% &7as a cheater and has now been removed.";

    @ConfigSetting(path = "banwave.message", name = "complete")
    public String completed = "&8[&6&lKauri&8] &eJudgement Day &7 has been completed.";
}
