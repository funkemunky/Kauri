package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

@CheckInfo(name = "BadPackets (Type J)", description = "finds your french baget.", type = CheckType.BADPACKETS, executable = false)
@Init
@Packets(packets = {Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_LOOK, Packet.Client.FLYING, Packet.Client.LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class BadPacketsJ extends Check {

    private long lastTimeStamp;
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        long delta = timeStamp - lastTimeStamp;

        if (getData().getLastServerPos().hasNotPassed(0) || getData().getLastLogin().hasNotPassed(20)) return;

        if(delta < 5) {
            if(verbose.flag(50, 200L)) {
                flag("t: " + verbose.getVerbose() + "; delta: " + delta + "ms", true, true);
            }
        } else verbose.setVerbose(0);

        debug(verbose.getVerbose() + ", " + delta);
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
