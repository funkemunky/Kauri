package cc.funkemunky.anticheat.impl.pup.bot;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import org.bukkit.scheduler.BukkitRunnable;

@Packets(packets = {Packet.Client.FLYING})
public class ConsoleClient extends AntiPUP {

    @Setting(name = "kickMessage")
    private String message = "&cConsole clients are not allowed.\n&7&oNot a console client? &fDon't freeze your game!";

    public ConsoleClient(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private long lastFlying;
    private int vl;
    private boolean kicked;
    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        long delta = timeStamp - lastFlying;

        if(delta > 200) {
            if(vl++ > 120 && !kicked) {
                new BukkitRunnable() {
                    public void run() {
                        kicked = true;
                        getData().getPlayer().kickPlayer(Color.translate(message));
                    }
                }.runTask(Kauri.getInstance());
            }
        } else vl = 0;
        lastFlying = timeStamp;
        return false;
    }
}
