package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type A)", description = "Checks for common denominators in the pitch.", cancelType = CancelType.MOTION, type = CheckType.AIM)
public class AimA extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getLastAttack().hasNotPassed(4)) {
            val move = getData().getMovementProcessor();

            if (Math.abs(move.getTo().getPitch()) < 88.0f
                    && move.getPitchDelta() > 0
                    && move.getYawDelta() < 10
                    && move.getOptifineTicks() < 2
                    && move.getPitchGCD() < 131072L) {
                if(vl++ > 60) {
                    flag(String.valueOf(move.getPitchGCD() / 2000), true, true, AlertTier.CERTAIN);
                } else if (vl > 40) {
                    flag(String.valueOf(move.getPitchGCD() / 2000), true, true, AlertTier.HIGH);
                } else if(vl > 30) {
                    flag(String.valueOf(move.getPitchGCD() / 2000), true, false, AlertTier.LIKELY);
                }
            } else {
                vl -= vl > 0 ? 2 : 0;
            }

            debug("VL: " + vl + " PITCH: " + move.getPitchGCD() + " OPTIFINE: " + getData().isCinematicMode());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
