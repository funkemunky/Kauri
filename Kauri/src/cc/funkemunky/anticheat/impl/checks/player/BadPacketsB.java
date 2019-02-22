package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_LOOK})
public class BadPacketsB extends Check {
    public BadPacketsB(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        switch (packetType) {
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LEGACY_LOOK:
            case Packet.Client.LEGACY_POSITION_LOOK:

                val data = getData();

                if (data.isLagging() || data.getLastServerPos().hasNotPassed(4))
                    return;

                val from = data.getMovementProcessor().getFrom();
                val to = data.getMovementProcessor().getTo();

                val yawDiff = Math.abs(from.getYaw() - to.getYaw());
                val pitchDiff = Math.abs(from.getPitch() - to.getPitch());

                // Some stupid clients actually do this
                if (yawDiff == 0.0 && pitchDiff == 0.0) {
                    if(vl++ > 4) {
                        this.flag("0|0", true, true);
                    }
                } else {
                    vl -= vl > 0 ? 1 : 0;
            }
        }
        return;
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
