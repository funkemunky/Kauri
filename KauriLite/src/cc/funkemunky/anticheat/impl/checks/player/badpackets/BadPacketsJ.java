package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.event.Event;

@CheckInfo(name = "BadPackets (Type J)", description = "finds your french baget.", type = CheckType.BADPACKETS, executable = false, developer = true)
//@Init
@Packets(packets = {Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_LOOK, Packet.Client.FLYING, Packet.Client.LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class BadPacketsJ extends Check {

    private long lastOverAmount, overAmountTicks, lastTimeStamp;
    private int ticks;
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        long delta = timeStamp - lastTimeStamp;

        if(getData().getMovementProcessor().isServerPos() || getData().getLastLogin().hasNotPassed(20)) return;

        if(delta > 60) {
            lastOverAmount = timeStamp;
            overAmountTicks = delta / 50;
        } else if(delta < 5) {
            if(ticks++ > overAmountTicks + 1) {
                if(verbose.flag(4, 2000L)) {
                    flag("fake lag", true, true);
                }
            }
        } else {
            if(verbose.getVerbose() < 1) {
                ticks = 0;
            }
            verbose.deduct();
        }

        debug(overAmountTicks + ", " + ticks + ", " + delta);
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
