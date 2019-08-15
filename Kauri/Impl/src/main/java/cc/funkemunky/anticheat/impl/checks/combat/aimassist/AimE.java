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

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
@Init
@CheckInfo(name = "Aim (Type E)", description = "Checks for low common denominators in other rotations.", type = CheckType.AIM, maxVL = 50)
public class AimE extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        long threshold = move.getYawDelta() > 10 ? 30000 : 60000;

        if(move.getYawDelta() == move.getLastYawDelta() || Math.abs(move.getTo().getPitch()) == 90) return;

        if(move.getYawGCD() < threshold && move.getYawDelta() > 0.3 && (move.getYawDelta() > 0.6 || move.getYawGCD() != move.getLastYawGCD()) && !getData().isCinematicMode()) {
            if(vl++ > 30) {
                flag("yaw=" + move.getYawGCD() + " vl=" + vl + " yd=" + move.getYawDelta(), true, true, vl > 50 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl-= vl > 0 ? (getData().isCinematicMode() || move.getYawGCD() == move.getLastYawGCD() ? 1 : 0.25) : 0;

        debug("yaw=" + move.getYawGCD() + " vl=" + vl + " cinematic=" + getData().isCinematicMode() + " yaw=" + move.getCinematicYawDelta() + " pitch=" + move.getCinematicPitchDelta());

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
