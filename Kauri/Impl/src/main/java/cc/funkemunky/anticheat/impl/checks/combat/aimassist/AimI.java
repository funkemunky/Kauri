package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@Init
@CheckInfo(name = "Aim (Type I)", description = "Ensures that pitch acceleration is legitimate.", maxVL = 50, type = CheckType.AIM)
public class AimI extends Check {

    private double vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(MathUtils.approxEquals(1E-5, move.getPitchDelta(), move.getLastPitchDelta()) && move.getPitchDelta() > 0.4) {
            if((vl = Math.min(20, vl + 1)) > 7) {
                flag("pitch=" + move.getPitchDelta() + " last=" + move.getLastPitchDelta() + " vl=" + vl, true, true, vl > 15 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl-= vl > 0 ? 0.25 : 0;
        debug("pitch=" + move.getPitchDelta() + " last=" + move.getLastPitchDelta() + " vl=" + vl);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
