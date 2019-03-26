package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type G)", description = "Checks for common denominators in the yaw.", type = CheckType.AIM, executable = false, cancellable = false, maxVL = 125)
public class AimG extends Check {
    public AimG() {

    }

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val offset = 16777216L;
        val gcd = MiscUtils.gcd((long) (move.getYawDelta() * offset), (long) (move.getLastYawDelta() * offset));
        val acceleration = MathUtils.getDelta(move.getLastYawDelta(), move.getYawDelta());

        if (Math.abs(move.getTo().getPitch()) < 88.0f && move.getYawDelta() > 0 && acceleration > 0 && getData().getMovementProcessor().getOptifineTicks() < 10 && gcd < 131072L) {
            if (vl++ > 100) {
                flag(String.valueOf(gcd / 2000), true, true);
            }
        } else {
            vl -= vl > 0 ? 3 : 0;
        }

        debug("VL: " + vl + " YAW: " + gcd + " OPTIFINE: " + getData().isCinematicMode());
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
