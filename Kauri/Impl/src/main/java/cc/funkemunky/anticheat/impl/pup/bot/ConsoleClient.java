package cc.funkemunky.anticheat.impl.pup.bot;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import org.bukkit.scheduler.BukkitRunnable;

@Packets(packets = {Packet.Client.FLYING, Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_LOOK, Packet.Client.KEEP_ALIVE})
public class ConsoleClient extends AntiPUP {

    @Setting(name = "kickMessage")
    private String message = "&cConsole clients are not allowed.\n&7&oNot a console client? &fDon't freeze your game!";

    public ConsoleClient(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private long lastFlying;
    private int vl = 0;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equalsIgnoreCase(Packet.Client.KEEP_ALIVE)) {
            if (timeStamp - lastFlying > 8000L && !getData().getPlayer().isDead() && getData().getLastLogin().hasPassed(10)) {
                if (vl++ > 2) {
                    new BukkitRunnable() {
                        public void run() {
                            getData().getPlayer().kickPlayer(Color.translate(message));
                        }
                    }.runTask(Kauri.getInstance());
                }
            } else vl = 0;
        } else lastFlying = timeStamp;
        return false;
    }
}
