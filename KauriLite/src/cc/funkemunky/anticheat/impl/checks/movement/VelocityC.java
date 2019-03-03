package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class VelocityC extends Check {
    public VelocityC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private double vl;

    //TODO Test
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val player = getData().getPlayer();
        val velocity = getData().getVelocityProcessor();
        val move = getData().getMovementProcessor();
        val hasTakenVelocity = velocity.getLastVelocity().hasNotPassed(8);

        if(!hasTakenVelocity) return;
        val colliding = getData().getBoundingBox().grow(1,0,1).add(0,0.00000001f,0,0,0.25f,0).getCollidingBlocks(player).stream().anyMatch(block -> !block.getType().equals(Material.AIR));

        if(!colliding) {
            val deltaXZ = move.getDeltaXZ();
            val velocityXZ = cc.funkemunky.anticheat.api.utils.MiscUtils.hypot(velocity.getMotionX(), velocity.getMotionZ());

            if(velocityXZ < 1E-4) return;
            val ratio = Math.abs(deltaXZ - velocityXZ);
            if(ratio < 0.5) {
                if(vl++ > 10) {
                    flag("velocity: " + ratio + "%", true, true);
                }
            } else vl-= vl > 0 ? 0.6 : 0;

            debug("VL: " + vl + " RATIO: " + ratio + " DELTAXZ: " + deltaXZ + " VELOCITYXZ: " + velocityXZ);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
