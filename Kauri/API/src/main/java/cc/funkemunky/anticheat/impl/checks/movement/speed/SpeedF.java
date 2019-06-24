package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Speed (Type F)", type = CheckType.SPEED, maxVL = 50)
@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class SpeedF extends Check {
    private float motionXZ;
    private int vl, jumpticks;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float decel = move.isServerOnGround() ? 0.9f : 0.984f;

        motionXZ = Math.max(MiscUtils.getBaseSpeed(getData()) + 0.046f, motionXZ * decel);

        if(move.getAirTicks() == 1) {
            motionXZ = 2.75f * MiscUtils.getBaseSpeed(getData());
            jumpticks = 2;
        } else if((jumpticks-= jumpticks > 0 ? 1 : 0) == 0) {
            jumpticks = -1;
            motionXZ/= 1.65;
        }

        if(move.getGroundTicks() == 1) {
            motionXZ *= 2;
        }

        float max = motionXZ + account();

        if(move.getDeltaXZ() > 0 && getData().getLastLogin().hasPassed(20) && !getData().isServerPos() && !getData().getPlayer().getAllowFlight() && getData().getPlayer().getVehicle() == null && move.getLastRiptide().hasPassed(20) && !move.isTookVelocity() && !PlayerUtils.isGliding(getData().getPlayer())) {
            if(move.getDeltaXZ() > max) {
                flag(move.getDeltaXZ() + ">-" + max, true, true, AlertTier.HIGH);
            }
            debug(move.getDeltaXZ() + ", " + motionXZ + ", " + max);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private float account() {
        float total = 0;

        val move = getData().getMovementProcessor();

        total += move.getIceTicks() > 0 ? .25 : 0;
        total += (getData().getPlayer().getWalkSpeed() - 0.2) * 2.4;
        total += (getData().getLastBlockPlace().hasNotPassed(7)) ? 0.2 : 0;
        total += move.isOnSlimeBefore() ? 0.18 : 0;
        total += move.getBlockAboveTicks() > 0 ? move.getIceTicks() > 0 ? 0.4 : 0.25 : 0;
        total += move.getHalfBlockTicks() > 0 ? 0.18 : 0;
        return total;
    }
}
