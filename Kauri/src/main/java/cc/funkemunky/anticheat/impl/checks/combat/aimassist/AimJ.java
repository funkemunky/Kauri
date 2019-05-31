package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Aim (Type M)", description = "A heuristic that looks for common patterns by advanced AimAssists (not stable) - FlyCode", type = CheckType.AIM, cancellable = false, executable = false)
@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK})
public class AimJ extends Check {

    private int count;
    private long lastGcd, ace;

    private float lastPitch, lastYaw;

    private Verbose verbose = new Verbose();

    @Setting(name = "thershold.count")
    private int countMax = 10;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (getData().getTarget() != null) {
            if (getData().isLagging()) {
                count = 0;
                lastGcd = 0;
                return;
            }
            if (move.getOptifineTicks() > 12) {
                count = 0;
                lastGcd = 0;
                return;
            }
            if ((move.getYawZeroTicks() > 2 ? (move.getPitchZeroTicks() * move.getYawZeroTicks()) : move.getYawZeroTicks()) > 3) {
                ace = timeStamp;
            }
            if (timeStamp - ace <= 100L) {
                if (count > 0) count--;
            }
            if (getData().getMovementProcessor().getDeltaXZ() < 0.1) {
                if (count > 0) count--;
            } else if (move.getYawZeroTicks() >= 5) {
                count = 0;
            }
            if (getData().getLastAttack().hasNotPassed(10)) {

                val offset = 16777216L;

                long p1 = (long) (move.getPitchDelta() * offset), p2 = (long) (move.getLastPitchDelta() * offset), gcd = MiscUtils.gcd(p1, p2);

                if (gcd == lastGcd) {
                    count += 2;
                } else {
                    if (count > 0) count -= 2;
                }
                if (Math.abs(move.getTo().getPitch() - move.getFrom().getPitch()) <= 0.5) {
                    if (move.getYawZeroTicks() > 1) {
                        count = 0;
                        return;
                    }
                    if (move.getDeltaXZ() < 0.20) {
                        if (count > 0) count--;
                    }
                    if (count > countMax && verbose.flag(4, 999L)) {
                        flag("test", true, true, AlertTier.POSSIBLE);
                    }
                    lastGcd = gcd;
                }
            }
        }

        debug("count: " + count + ", " + getData().getMovementProcessor().getOptifineTicks() + ", " + (timeStamp - ace) + ", " + move.getYawZeroTicks() + ", " + move.getPitchZeroTicks());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
