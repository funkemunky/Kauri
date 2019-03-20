package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatSpam extends AntiPUP {
    public ChatSpam(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private long lastMessage;
    private int vl;

    @Setting(name = "threshold.spamCount")
    private int spamMax = 3;

    @Setting(name = "threshold.chatDelay")
    private long chatDelay = 500L;

    @Setting(name = "noSpamMessage")
    private String message = "&8[&4!&8] &7You must wait &c%timeLeft% seconds &7 until you can chat.";

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        return false;
    }

    @EventHandler
    public void onEvent(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        if (!message.startsWith("/")) {
            long timeStamp = System.currentTimeMillis();
            long delta = timeStamp - lastMessage;
            if (delta < chatDelay) {
                if (vl++ > spamMax) {
                    double seconds = MathUtils.round(delta / 1000D, 1);

                    event.getPlayer().sendMessage(message);
                    event.setCancelled(true);
                }
            } else vl -= vl > 0 ? 1 : 0;
            lastMessage = timeStamp;
        }
    }
}
