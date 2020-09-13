package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Aim (F)", description = "An angle deviation check made by DeprecatedLuke.", checkType = CheckType.AIM,
        punishVL = 20, developer = true, enabled = false)
public class AimF extends Check {

    private float lastPitch = -1;
    private long[] gcdLog = new long[10];
    private int current = 0;

    private MaxDouble verbose = new MaxDouble(40);

    @Packet
    public void check(WrappedInFlyingPacket packet) {
        if (!packet.isLook()) return;

        Vector first = new Vector(data.playerInfo.deltaYaw, 0, data.playerInfo.deltaPitch);
        Vector second = new Vector(data.playerInfo.lDeltaYaw, 0, data.playerInfo.lDeltaPitch);

        double angle = Math.pow(first.angle(second) * 180, 2);

        long deviation = getDeviation(data.playerInfo.deltaPitch);

        gcdLog[current % gcdLog.length] = deviation;
        current++;

        float yawDif = Math.abs(data.playerInfo.deltaYaw - data.playerInfo.lDeltaYaw);

        if (Math.abs(data.playerInfo.deltaPitch) > 0
                && data.playerInfo.deltaYaw > 1
                && data.playerInfo.deltaYaw < 10
                && data.playerInfo.lDeltaYaw <= data.playerInfo.deltaYaw
                && yawDif != 0
                && yawDif < 3
                && !data.playerInfo.cinematicMode
                && angle > 10000
        )
        {

            if (current > gcdLog.length) {
                long maxDeviation = 0;
                for (long l : gcdLog) if (deviation != 0 && l != 0)
                    maxDeviation = Math.max(Math.max(l, deviation) % Math.min(l, deviation), maxDeviation);
            }
            if (deviation > 0) {
                if(verbose.add() > 2) {
                    vl++;
                    flag("y1=%v,y2=%v,a=%v", data.playerInfo.deltaYaw, yawDif, angle);
                }
                reset();
            }
        }
        verbose.subtract(0.001);
        debug("y1=%v,y2=%v,a=%v,vb=%v,dev=%v", data.playerInfo.deltaYaw, yawDif, angle, verbose.value(), deviation);
    }

    public long getDeviation(float pitchChange) {
        if (lastPitch != -1) {
            try {
                long current = (long) (pitchChange * MovementProcessor.offset);
                long last = (long) (lastPitch * MovementProcessor.offset);
                long value = convert(current, last);

                if (value < 0x20000) {
                    return value;
                }
            } catch (Exception e) {
            }
        }

        lastPitch = pitchChange;
        return -1;
    }

    public void reset() {
        lastPitch = -1;
    }

    private long convert(long current, long last) {
        if (last <= 16384) return current;
        return convert(last, current % last);
    }
}
