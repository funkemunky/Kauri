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
import lombok.Getter;
import lombok.var;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@CheckInfo(name = "Velocity (D)", description = "Checks if a player responded to velocity",
        checkType = CheckType.VELOCITY, punishVL = 5, planVersion = KauriVersion.FREE, devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class VelocityD extends Check {

    private int buffer;
    private final Timer lastVelocity = new TickTimer();
    private final List<Velocity> velocityY = new CopyOnWriteArrayList<>();

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
                .map(t -> new Tuple<>(t, velocityY.indexOf(t)))
                .collect(Collectors.toList());

        if(toRemove.size() > 0) {

            if(buffer > 0) buffer--;
            debug("Reset velocity: dy=%.4f b=%s",
                    data.playerInfo.deltaY, buffer);

            //Removing
            for (Tuple<Velocity, Integer> i : toRemove) {
                toRemove.remove((int)i.two);
            }
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
                .map(t -> new Tuple<>(t, velocityY.indexOf(t)))
                .collect(Collectors.toList());

        if(toRemove.size() > 0) {
            List<String> velocities = new ArrayList<>();
            for (Tuple<Velocity, Integer> i : toRemove) {
                velocities.add(String.valueOf(i.one.getVelocity()));
                velocityY.remove((int)i.two);
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
