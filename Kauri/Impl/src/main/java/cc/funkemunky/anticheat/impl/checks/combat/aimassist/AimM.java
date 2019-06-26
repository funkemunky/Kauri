package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.HashSet;
import java.util.Set;

@Init
@CheckInfo(name = "Aim (Type M)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimM extends Check {

    private double vl;
    private float lastAccel;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val accelYaw = MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta());
        val delta = MathUtils.getDelta(accelYaw, lastAccel);
        /*val pitchGCD = MiscUtils.gcd((long) (16777216L * move.getPitchDelta()), (long) (16777216L * move.getLastPitchDelta())) / 16777216F;

        if((MathUtils.getDelta(accelYaw, yawGCD) < 1E-5 || (MathUtils.getDelta(accelPitch, pitchGCD) < 1E-5 && accelPitch > pitchGCD * 2)) && move.getYawDelta() > 0.7 && getData().isCinematicMode() && delta > 0.01) {
            debug(Color.Green + "Flag: " + vl++);
            debug("[" + accelYaw + ", " + yawGCD + "] , [" + accelPitch + ", " + pitchGCD + "] " + move.getYawDelta());
        } else vl-= vl > 0 ? 0.25 : 0;*/

        if(delta > 1E-4 && getData().isCinematicMode() && move.getPitchDelta() == 0 && move.getYawDelta() > 0.8) {
            if(vl++ > 30) {
                flag("test", true, true, AlertTier.HIGH);
            }
            debug(Color.Green + "VL: " +  vl);
        } else vl-= vl > 0 ? 0.25 : 0;
        lastAccel = accelYaw;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
