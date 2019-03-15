package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Setting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandSpam extends AntiPUP {
    public CommandSpam(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    @Setting(name = "minimumCommandDelay")
    private long minimumDelay = 450L;

    long lastPreprocess;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        return false;
    }

    @EventHandler
    public void onPreprocessCommand(PlayerCommandPreprocessEvent event) {
        long timeStamp = System.currentTimeMillis();
        if(timeStamp - lastPreprocess < minimumDelay) {
            event.setCancelled(true);
        }
        lastPreprocess = timeStamp;
    }
}
