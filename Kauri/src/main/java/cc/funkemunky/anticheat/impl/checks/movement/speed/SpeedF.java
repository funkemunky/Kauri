package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.*;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Speed (Type F)", type = CheckType.SPEED, maxVL = 50)
@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class SpeedF extends Check {
    private float motionXZ;
    private boolean lastjump;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float decel = 0.984f;

        if(move.isClientOnGround()) {
            decel*= ReflectionsUtil.getFriction(BlockUtils.getBlock(move.getTo().toLocation(getData().getPlayer().getWorld()).clone().subtract(0,1,0)));
        }

        motionXZ = Math.max(MiscUtils.getBaseSpeed(getData()) + 0.046f, motionXZ * decel);

        if(move.isHasJumped()) {
            motionXZ = 2.75f * MiscUtils.getBaseSpeed(getData());
            lastjump = true;
        } else if(lastjump) {
            lastjump = false;
            motionXZ/= 1.65;
        }

        if(move.getGroundTicks() < 2 && move.getAirTicks() == 0) {
            motionXZ *= 2;
        }

        if(move.getDeltaXZ() > 0 && !getData().isServerPos() && !getData().getPlayer().getAllowFlight() && getData().getPlayer().getWalkSpeed() < 0.25 && getData().getPlayer().getVehicle() == null && move.getLastRiptide().hasPassed(20) && !move.isTookVelocity() && !PlayerUtils.isGliding(getData().getPlayer())) {
            if(move.getDeltaXZ() > motionXZ) {
                if(vl++ > 2 || move.getDeltaXZ() - motionXZ > 0.1) {
                    flag(move.getDeltaXZ() + ">-" + motionXZ, true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug(move.getDeltaXZ() + ", " + motionXZ);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
