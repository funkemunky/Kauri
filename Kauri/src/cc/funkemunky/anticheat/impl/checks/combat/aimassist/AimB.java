package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@CheckInfo(name = "Aim (Type B)", description = "Checks for common denominators in the pitch.", maxVL = 50, cancelType = CancelType.MOTION, type = CheckType.AIM)
public class AimB extends Check {
    public AimB(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);

        setDeveloper(true);
    }

    private float lastPitchDelta, lastYawDelta;
    private Verbose verbose = new Verbose();
    private long lastGCD;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val to = getData().getMovementProcessor().getTo();
        val from = getData().getMovementProcessor().getFrom();
        val pitchDifference = Math.abs(from.getPitch() - to.getPitch());
        val yawDifference = Math.abs(from.getYaw() - to.getYaw());

        val offset = 16777216L;
        val pitchGCD = MiscUtils.gcd((long) (pitchDifference * offset), (long) (lastPitchDelta * offset));

        if (Math.abs(to.getPitch()) < 88.0f && pitchDifference > 0 && getData().getMovementProcessor().getOptifineTicks() < 10 && (pitchGCD < 131072L || pitchGCD == lastGCD)) {
            if (verbose.flag(150, 5000L)) {
                flag(String.valueOf(pitchGCD / 2000), true, true);
            }
        } else verbose.deduct(2);

        debug("VL: " + verbose.getVerbose() + " PITCH: " + pitchGCD + " OPTIFINE: " + getData().isCinematicMode());

        lastPitchDelta = pitchDifference;
        lastYawDelta = yawDifference;
        lastGCD = pitchGCD;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
