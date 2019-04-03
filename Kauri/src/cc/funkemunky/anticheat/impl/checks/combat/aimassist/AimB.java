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
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type B)", description = "Checks for common denominators in the pitch.", maxVL = 50, cancelType = CancelType.MOTION, type = CheckType.AIM)
public class AimB extends Check {
    public AimB() {

    }

    private Verbose verbose = new Verbose();
    private long lastGCD;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val pitchDifference = getData().getMovementProcessor().getPitchDelta();
        val lastPitchDifference = getData().getMovementProcessor().getLastPitchDelta();
        val offset = 16777216L;
        val pitchGCD = MiscUtils.gcd((long) (pitchDifference * offset), (long) (lastPitchDifference * offset));

        if (Math.abs(getData().getMovementProcessor().getTo().getPitch()) < 88.0f && pitchDifference > 0 && getData().getMovementProcessor().getOptifineTicks() < 10 && (pitchGCD < 131072L || pitchGCD == lastGCD)) {
            if (verbose.flag(150, 5000L)) {
                flag(String.valueOf(pitchGCD / 2000), true, true);
            }
        } else verbose.deduct(2);

        debug("VL: " + verbose.getVerbose() + " PITCH: " + pitchGCD + " OPTIFINE: " + getData().isCinematicMode());

        lastGCD = pitchGCD;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
