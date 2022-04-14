package dev.brighten.anticheat.check.impl.free.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.Getter;
import lombok.var;

import java.util.*;
import java.util.stream.Collectors;

//@CheckInfo(name = "Velocity (D)", description = "Checks if a player responded to velocity",
//        checkType = CheckType.VELOCITY, punishVL = 5, planVersion = KauriVersion.FREE, devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class VelocityD extends Check {

    private int buffer;
    private final Timer lastVelocity = new TickTimer();
    private final List<Velocity> velocityY = Collections.synchronizedList(new ArrayList<>());

    @Setting(name = "bufferThreshold")
    private static int bufferThreshold = 3;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0.1) {
            data.runKeepaliveAction(ka -> {
                synchronized (velocityY) {
                    velocityY.add(new Velocity(packet.getY()));
                }
            });
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(velocityY.size() == 0) return;

        var toRemove = velocityY.stream()
                .filter(t -> Math.abs(data.playerInfo.deltaY - t.getVelocity()) < 0.001)
                .iterator();

        while(toRemove.hasNext()) {
            Velocity velocity = toRemove.next();

            if(buffer > 0) buffer--;
            debug("Reset velocity: dy=%.4f vy=%.4f b=%s",
                    data.playerInfo.deltaY, velocity.getVelocity(), buffer);

            //Removing
            toRemove.remove();

        }

        //All potential causes of false positives
        if(data.playerInfo.doingBlockUpdate
                || data.playerInfo.webTimer.isNotPassed(3)
                || data.playerInfo.liquidTimer.isNotPassed(3)
                || data.playerInfo.slimeTimer.isNotPassed(2)
                || data.blockInfo.inScaffolding
                || data.blockInfo.inHoney
                || data.playerInfo.blockAboveTimer.isNotPassed(2)) {
            debug("Potential false flag");
            return;
        }

        toRemove = velocityY.stream().filter(t -> now - t.getTimestamp() > 500)
                .iterator();

        if(toRemove.hasNext()) {
            List<String> velocities = new ArrayList<>();
            while(toRemove.hasNext()) {
                velocities.add(String.valueOf(toRemove.next().getVelocity()));
                toRemove.remove();
            }
            if(++buffer > 2) {
                vl++;
                flag("lv=%s;v=[%s]", lastVelocity.getPassed(), String.join(";", velocities));
            }
        }
    }

    @Getter
    public static class Velocity {
        private final double velocity;
        private final long timestamp;

        public Velocity(double velocityY) {
            this.velocity = velocityY;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
