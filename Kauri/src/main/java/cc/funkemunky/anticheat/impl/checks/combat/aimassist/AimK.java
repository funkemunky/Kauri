package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Aim (Type K)", description = "Looks for rounded rotational values", type = CheckType.AIM, cancelType = CancelType.MOTION, developer = true, executable = false)
@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
public class AimK extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (getData().isServerPos()) return;

        if (move.getYawDelta() > 0.1 && move.getYawDelta() % 0.5 == 0 || move.getTo().getYaw() % 1 == 0) {
            if (verbose.flag(6, 600L)) {
                flag("YD: " + move.getYawDelta() + " YAW: " + move.getTo().getYaw(), true, true, AlertTier.HIGH);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
