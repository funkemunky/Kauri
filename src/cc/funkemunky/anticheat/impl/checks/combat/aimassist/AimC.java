package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class AimC extends Check {
    public AimC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);

        setDeveloper(true);
    }

    private float lastPitchDelta, lastYawDelta;
    private double vl;
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
            if(vl++ > 150) {
                flag(String.valueOf(pitchGCD / 2000), true, true);
            }
        } else {
            vl -= vl > 0 ? 2 : 0;
        }

        debug("VL: " + vl + " PITCH: " + pitchGCD + " OPTIFINE: " + getData().isCinematicMode());

        lastPitchDelta = pitchDifference;
        lastYawDelta = yawDifference;
        lastGCD = pitchGCD;
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
