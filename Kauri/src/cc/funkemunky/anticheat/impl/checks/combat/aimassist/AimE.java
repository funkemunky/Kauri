package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

public class AimE extends Check {
    public AimE(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private int vl;

    @Setting(name = "threshold.verbose.max")
    private int vlMax = 40;

    @Setting(name = "threshold.verbose.subtract")
    private int vlSub = 2;

    @Setting(name = "threshold.yawAccel")
    private double yawAccelMax = 1E-5;

    @Setting(name = "threshold.pitchAccel")
    private double pitchAccelMax = 1E-7;

    @Setting(name = "threshold.minYawDelta")
    private double minYawDelta = 0.6;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val yawDelta = move.getYawDelta();
        val pitchDelta = move.getPitchDelta();
        val yawAccel = MathUtils.getDelta(yawDelta, move.getLastYawDelta());
        val pitchAccel = MathUtils.getDelta(pitchDelta, move.getLastPitchDelta());

        if(yawDelta > minYawDelta && (pitchAccel < pitchAccelMax || yawAccel < yawAccelMax)) {
            if(vl++ > vlMax) {
                flag("YAW: " + MathUtils.round(yawAccel, 3) + " PITCH: " + MathUtils.round(pitchAccel, 3), true, true);
            }
        } else vl-= vl > 0 ? vlSub : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
