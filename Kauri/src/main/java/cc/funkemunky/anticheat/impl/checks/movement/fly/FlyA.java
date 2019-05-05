package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;


@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fly (Type A)", description = "A simple acceleration check for flight.", type = CheckType.FLY, cancelType = CancelType.MOTION, maxVL = 150)
public class FlyA extends Check {

    private int verbose;
    private Verbose verboseLow = new Verbose();
    private long lastTimeStamp;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.cancelForFlight(getData())) return;
        val move = getData().getMovementProcessor();

        /* This checks for the acceleration of the player being too low. The average acceleration for a legitimate player is around 0.08.
           We check if it's less than 1E-4 for some compensation of inconsistencies that happen very often due to netty.
         */

        if (timeStamp - lastTimeStamp > 12) {
            if (move.getAirTicks() > 2
                    && Math.abs(move.getClientYAcceleration()) < 1E-5) {
                if (verboseLow.flag(9, 800)) {
                    flag("t: low; " + move.getDeltaY() + "≈" + move.getLastDeltaY(), true, true);
                }
            }

            /* This is to check for large amounts of instant acceleration to counter any fly which tries bypass in this manner  */
            if (Math.abs(move.getClientYAcceleration()) > 0.1
                    && !move.isBlocksOnTop()) {
                //We have to add a verbose since this check isn't 100% accurate and therefore can have issues.
                //However, we can instantly flag if they are already in the air since a large delta between velocities is impossible.
                if (verbose++ > 3) flag("t: high; " + move.getDeltaY() + ">-" + move.getLastDeltaY(), true, true);
            } else verbose = 0;
            debug(move.isServerOnGround() + ", " + move.isBlocksOnTop() + ", " + Math.abs(move.getServerYAcceleration()) + ", " + move.getServerYVelocity() + ", " + move.getDeltaY() + ", " + move.getDistanceToGround());
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {
    }
}