package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
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
@CheckInfo(name = "Killaura (Type I)", description = "Checks for another overall flaw in the rotations of many killauras.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, maxVL = 200)
public class KillauraI extends Check {
    public KillauraI(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getLastAttack().hasNotPassed(4)) {
            val move = getData().getMovementProcessor();

            val offset = 16777216L;
            val gcd = MiscUtils.gcd((long) (move.getYawDelta() * offset), (long) (move.getLastYawDelta() * offset));
            val acceleration = MathUtils.getDelta(move.getLastYawDelta(), move.getYawDelta());

            if (Math.abs(move.getTo().getPitch()) < 88.0f && move.getYawDelta() > 0 && acceleration > 0 && getData().getMovementProcessor().getOptifineTicks() < 10 && gcd < 131072L) {
                if (vl++ > 100) {
                    flag(String.valueOf(gcd / 2000), true, true);
                }
            } else {
                vl -= vl > 0 ? 2 : 0;
            }

            debug("VL: " + vl + " YAW: " + gcd + " OPTIFINE: " + getData().isCinematicMode());
        }
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
