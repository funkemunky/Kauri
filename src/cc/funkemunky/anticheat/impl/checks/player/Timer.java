package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class Timer extends Check {

    private Verbose verbose = new Verbose();
    private int timerTicks;
    private long time;
    public Timer(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getLastServerPos().hasPassed(2) && getData().getLastLogin().hasPassed(40)) {
            if (System.currentTimeMillis() - time >= 1000L) {
                if (timerTicks > 21) {
                    if (verbose.flag(3, 2500L)) {
                        flag(timerTicks + ">-21", false, true);
                    }
                }
                debug(verbose.getVerbose() + ": " + timerTicks);
                timerTicks = 0;
                time = System.currentTimeMillis();
            } else if (!getData().isLagging()) {
                timerTicks++;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}