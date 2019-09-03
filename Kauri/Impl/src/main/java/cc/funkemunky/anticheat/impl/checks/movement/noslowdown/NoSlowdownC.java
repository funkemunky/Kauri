package cc.funkemunky.anticheat.impl.checks.movement.noslowdown;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "NoSlowdown (Type C)", description = "Ensures the player is not moving too fast in webs.", executable = false, developer = true)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class NoSlowdownC extends Check {

    private Verbose verbose = new Verbose(), verbose2 = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val baseSpeed = move.getBaseSpeed() - 0.065f;
        val accelerationMax = 0.05f;
        val accel = Math.abs(move.getClientYAcceleration());

        if (move.isInWeb() && move.getWebTicks() > 4) {
            if (accel > accelerationMax) {
                if (verbose.flag(4, 500L)) {
                    flag(MathUtils.round(accel, 4) + ">-" + accelerationMax, true, true, AlertTier.HIGH);
                }
            } else verbose.deduct();

            if (move.getDeltaXZ() > baseSpeed) {
                if (verbose2.flag(6, 500L)) {
                    flag(MathUtils.round(move.getDeltaXZ(), 4) + ">-" + baseSpeed, true, true, AlertTier.HIGH);
                }
            } else verbose2.deduct();

            debug("[" + verbose.getVerbose() + "," + verbose2.getVerbose() + "]: " + accel + ", " + move.getDeltaXZ());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
