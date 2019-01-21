package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class SpeedB extends Check {
    private int verboseA, verboseB, verboseC;
    private float lastMotionXZ;
    private long lastTimeStamp;
    public SpeedB(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        //The client will always send a position packet when teleported or dictated to move by the server, so we need to account for that to prevent false-positives.
        if (getData().getLastServerPos().hasNotPassed(1) || getData().isGeneralCancel()) {
            verboseA = verboseB = verboseC = 0;
            return packet;
        }
        val to = getData().getMovementProcessor().getTo();
        val from = getData().getMovementProcessor().getFrom();

        /* We we do just a basic calculation of the maximum allowed movement of a player */
        float motionXZ = (float) Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()), acceleration = motionXZ - lastMotionXZ;

        if (timeStamp - lastTimeStamp > 1) {
            /* This checks if the horizontal velocity of the player increases while in the air, which is impossible with a vanilla client
             * We use this as a counter to a potential verbose bypass (similar to one for Janitor) for the check above. */

            if (acceleration > 0.16
                    && getData().getMovementProcessor().getAirTicks() > 3 //We want to make sure the player is in the air and not jumping.
                    && !getData().getMovementProcessor().isInLiquid()
                    && getData().getMovementProcessor().getClimbTicks() == 0) { //A player in liquid can register as though he/she is in the air.
                if (verboseB++ > 3) {
                    flag("t: high;" + motionXZ + ">-" + lastMotionXZ, true, false);
                }
            } else {
                verboseB = 0;
            }

        /* This checks if the speed has consistent deceleration or acceleration. Whenever a player moves in the air, the player must
           always decelerate at a constant rate.
         */
            if (Math.abs(acceleration) < 1E-4
                    && motionXZ > 0
                    && getData().getMovementProcessor().getClimbTicks() == 0
                    && getData().getLastBlockPlace().hasPassed(4)
                    && getData().getMovementProcessor().getAirTicks() < 20 //This is a light and quick fix for the horizontal velocity being constant legitimately.
                    && getData().getMovementProcessor().getAirTicks() > 3) { //We check 3 instead of 2 for airTicks as an added measure. This is for the same reason as the previous detection.
                if (verboseA++ > 8) { //We have to add a slight verbose due to the occasional hiccup in the flow of packets.
                    flag("t: low; " + motionXZ + "≈" + lastMotionXZ, true, false);
                }
            } else {
                verboseA = 0;
            }

            if (Math.abs(acceleration) > 0.25
                    && getData().getMovementProcessor().isServerOnGround()
                    && getData().getMovementProcessor().getClimbTicks() == 0) {
                if (verboseC++ > 2) {
                    flag("t: ground; " + acceleration + ">-0.2", true, false);
                }
            } else {
                verboseC = 0;
            }

        }
        lastMotionXZ = motionXZ;
        lastTimeStamp = timeStamp;
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
