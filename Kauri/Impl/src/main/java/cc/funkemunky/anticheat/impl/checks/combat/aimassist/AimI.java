package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.TickTimer;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Set;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type I)", maxVL = 50, type = CheckType.AIM)
public class AimI extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getLastPitchDelta() == move.getPitchDelta() && move.getPitchDelta() > 0.1) {
            if(vl++ > 8) {
                flag("pitch=" + move.getPitchDelta() + " vl=" + vl, true, true, vl > 14 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl-= vl > 0 ? 0.25 : 0;
        debug("pitch=" + move.getPitchDelta() + " yaw=" + move.getYawDelta() + " vl=" + vl);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
