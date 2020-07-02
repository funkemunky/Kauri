package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Aim (G)", description = "Checks if the yaw rotation snaps.",
        checkType = CheckType.AIM, punishVL = 10)
public class AimG extends Check {


    private float lastYaw;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (packet.isPos() || packet.isLook()) {

            //Grab the players yaw from the packet
            double yaw = packet.getYaw();

            //Grab the yaw delta from the last yaw and current yaw
            double yawDelta = Math.abs(yaw - this.lastYaw);

            //Remove the mouseX delta from the yawDelta
            double fix = (yawDelta - data.moveProcessor.deltaX);

            //Check if yawDelta is more than 0.0 and fix is more than 0.0
            if (yawDelta > 0.0 && fix > 0.0) {

                //Calculate the snap angle from yawDelta and fix
                double snap = Math.abs(yawDelta - fix);

                //Check if the player did snap onto a player
                if (snap == 0.0 && fix != 360) {

                    //Violate the player
                    flag("snap=%v", snap);
                }
            }

            //Store lastYaw from packet
            this.lastYaw = packet.getYaw();
        }
    }
}