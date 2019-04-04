package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

@CheckInfo(name = "BadPackets (Type H)", description = "Looks for a mistake commonly found in autoblock modules on cheat clients.", type = CheckType.BADPACKETS, cancelType = CancelType.INTERACT)
@Init
@Packets(packets = {Packet.Client.USE_ENTITY, Packet.Client.BLOCK_DIG})
public class BadPacketsH extends Check {

    @Setting(name = "theshold.vl.max")
    private int maxVL = 14;

    @Setting(name = "threshold.vl.reset")
    private long resetTime = 600L;

    @Setting(name = "threshold.vl.deduct")
    private int deduct = 1;

    private long blockDig, useEntity;
    private Verbose verbose = new Verbose();

    //TODO block place and block dig difference.
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Client.USE_ENTITY)) {
            if(timeStamp - useEntity < 5 || getData().isLagging()) return;
            long delta = (timeStamp - blockDig);

            if(delta == 0) {
                if(verbose.flag(maxVL, resetTime, 2)) {
                    flag(delta + "ms", true, true);
                }
            } else verbose.deduct(deduct);
            debug("USE: [" + (timeStamp - blockDig) + "ms]");
            useEntity = timeStamp;
        } else {
            blockDig = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
