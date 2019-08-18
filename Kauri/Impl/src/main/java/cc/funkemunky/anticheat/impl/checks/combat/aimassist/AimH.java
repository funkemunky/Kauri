package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type H)", description = "Designed to detect Vape's Aimassist.", type = CheckType.AIM, executable = true, maxVL = 10)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimH extends Check {

    private double vl;
    private float lastAccel, lastDelta;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float accel = Math.abs(move.getYawDelta() - move.getLastYawDelta());
        float delta = Math.abs(accel - lastAccel);
        float deltaAccel = Math.abs(accel - delta);

        if((move.getCinematicYawDelta() < 0.4 && move.getYawDelta() > 0.8 && accel > 0.1 && delta > 0.1)) {
            debug(Color.Green + "Flag");
        }

        debug("accel=" + accel + " delta=" + delta + " cinematic=" + move.getCinematicYawDelta() + " yd=" + move.getYawDelta());
        lastDelta = delta;
        lastAccel = accel;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}