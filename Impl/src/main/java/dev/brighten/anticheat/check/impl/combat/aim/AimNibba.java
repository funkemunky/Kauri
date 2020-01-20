package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Aim (Nibba)", description = "nibba check", checkType = CheckType.AIM, developer = true)
@Cancellable
public class AimNibba extends Check {

    private float lastYaw, lastPitch, lastGrid, lastGridDelta;
    private List<Float> yawSamples = new LinkedList<>(), pitchSamples = new LinkedList<>();
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (packet.isLook()) {
            float yaw = packet.getYaw();
            float pitch = packet.getPitch();

            float yawDelta = Math.abs(this.lastYaw - yaw);
            float pitchDelta = Math.abs(this.lastPitch - pitch);

            if (yawDelta > 0.0 && pitchDelta > 0.0) {
                this.yawSamples.add(yawDelta);
                this.pitchSamples.add(pitchDelta);

                if (this.yawSamples.size() == 20 || this.pitchSamples.size() == 20) {
                    float smallestPitch = Collections.min(pitchSamples);

                    AtomicInteger level = new AtomicInteger();

                    boolean flagged = false;
                    for (int i = 0; i < 20; i++) {
                        float currentGrid = this.getGrid(smallestPitch, yawSamples.get(i));
                        float gridDelta = Math.abs(currentGrid - lastGrid);

                        if (gridDelta > 0.0 && gridDelta != lastGridDelta) {
                            if (level.incrementAndGet() > 11) {
                                vl++;
                                flag("delta=%1", gridDelta);
                                flagged = true;
                            }
                        }

                        debug("delta=%1 vl=%2", gridDelta, vl);

                        this.lastGrid = currentGrid;
                        this.lastGridDelta = gridDelta;
                    }

                    if(!flagged) {
                        vl-= vl > 0 ? 0.2 : 0;
                    }

                    this.yawSamples.clear();
                    this.pitchSamples.clear();
                }
            }

            this.lastYaw = yaw;
            this.lastPitch = pitch;
        }
    }

    private float getGrid(float smallestPitch, float currentYaw) {
        return Math.abs((currentYaw + smallestPitch) % (currentYaw - smallestPitch));
    }
}
