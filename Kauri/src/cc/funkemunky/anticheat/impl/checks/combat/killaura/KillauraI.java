package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.event.Event;

public class KillauraI extends Check {
    public KillauraI(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);

        setDeveloper(true);
    }

    private float lastYawDelta;
    private double vl;
    private long lastGCD;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getLastAttack().hasNotPassed(4)) {
            val to = getData().getMovementProcessor().getTo();
            val from = getData().getMovementProcessor().getFrom();
            val pitchDifference = Math.abs(from.getPitch() - to.getPitch());
            val yawDifference = Math.abs(from.getYaw() - to.getYaw());

            val offset = 16777216L;
            val gcd = MiscUtils.gcd((long) (yawDifference * offset), (long) (lastYawDelta * offset));

            if (Math.abs(to.getPitch()) < 88.0f && pitchDifference > 0  && getData().getMovementProcessor().getOptifineTicks() < 10 && gcd < 131072L) {
                if(vl++ > 100) {
                    flag(String.valueOf(gcd / 2000), true, true);
                }
            } else {
                vl -= vl > 0 ? 3 : 0;
            }

            debug("VL: " + vl + " YAW: " + gcd + " OPTIFINE: " + getData().isCinematicMode());

            lastYawDelta = yawDifference;
            lastGCD = gcd;
        }
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
