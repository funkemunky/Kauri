package dev.brighten.anticheat.check.impl.free.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.val;
import lombok.var;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@CheckInfo(name = "Velocity (D)", description = "Checks if a player responded to velocity",
        checkType = CheckType.VELOCITY, punishVL = 5, planVersion = KauriVersion.FREE, devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class VelocityD extends Check {

    private int buffer;
    private double vY;
    private Timer lastVelocity = new TickTimer();
    private Set<Tuple<Double, Long>> velocityY = new HashSet<>();

    @Setting(name = "bufferThreshold")
    private static int bufferThreshold = 3;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0.1) {
            data.runKeepaliveAction(ka ->
                    velocityY.add(new Tuple<>(packet.getY(), System.currentTimeMillis())));
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(velocityY.size() == 0) return;

        var toRemove = velocityY.stream()
                .filter(t -> Math.abs(data.playerInfo.deltaY - t.one) < 1E-4)
                .collect(Collectors.toList());

        //Player took velocity
        if(toRemove.size() > 0) {
            toRemove.forEach(velocityY::remove);
            toRemove.clear();
            if(buffer > 0) buffer--;
            debug("Reset velocity: dy=%.4f vy=%.4f", data.playerInfo.deltaY, vY);
            vY = 0;
            return;
        }

        //All potential causes of false positives
        if(data.playerInfo.doingBlockUpdate
                || data.playerInfo.webTimer.isNotPassed(3)
                || data.playerInfo.liquidTimer.isNotPassed(3)
                || data.playerInfo.slimeTimer.isNotPassed(2)
                || data.blockInfo.inScaffolding
                || data.blockInfo.inHoney
                || data.playerInfo.blockAboveTimer.isNotPassed(2)) {
            vY = 0;
            debug("Potential false flag");
            return;
        }

        toRemove = velocityY.stream().filter(t -> now - t.two > 500)
                .collect(Collectors.toList());

        int timeCount = toRemove.size();

        if(timeCount > 0) {
            toRemove.forEach(velocityY::remove);
            toRemove.clear();
            if(++buffer > 2) {
                vl++;
                flag("lv=%s", lastVelocity.getPassed());
            }
            vY = 0;
        }
    }
}
