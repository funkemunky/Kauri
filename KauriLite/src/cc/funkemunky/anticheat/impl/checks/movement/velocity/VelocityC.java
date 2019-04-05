package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import lombok.var;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Packets(packets = {Packet.Server.ENTITY_VELOCITY, Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@CheckInfo(name = "Velocity (Type C)", description = "Predicts the minimum horizontal movement someone should move when velocity is taken.", type = CheckType.VELOCITY, cancelType = CancelType.MOTION, developer = true, executable = false)
@Init
public class VelocityC extends Check {

    private double vl;
    private double velocity;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if (velocity.getId() == velocity.getPlayer().getEntityId()) {
                this.velocity = MiscUtils.hypot(velocity.getX(), velocity.getZ());
            }
        } else {
            var getAIMoveSpeed = getData().getPlayer().getWalkSpeed() / 2;
            val player = getData().getPlayer();
            val action = getData().getActionProcessor();
            val move = getData().getMovementProcessor();
            val velocity = getData().getVelocityProcessor();

            val noneCollide = getData().getBoundingBox().grow(1.5f, 0, 1.5f).getCollidingBlocks(player).stream().noneMatch(block -> !block.isEmpty());

            if (this.velocity > 0 && !move.isBlocksOnTop() && noneCollide) {
                val velocityXZ = this.velocity;
                val ratio = move.getDeltaXZ() / velocityXZ;
                float max = 1;
                if (action.isSprinting())
                    getAIMoveSpeed += 0.03000001F;

                if (getData().getPlayer().hasPotionEffect(PotionEffectType.SPEED)) {
                    getAIMoveSpeed += (PlayerUtils.getPotionEffectLevel(player, PotionEffectType.SPEED) * (0.20000000298023224D)) * getAIMoveSpeed;
                }
                if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                    getAIMoveSpeed += (PlayerUtils.getPotionEffectLevel(player, PotionEffectType.SLOW) * (-0.15000000596046448D)) * getAIMoveSpeed;
                }
                getAIMoveSpeed += (player.getWalkSpeed() - 0.2) * 5 * 0.45;

                max /= (getAIMoveSpeed / 0.075) + (getData().getLastAttack().hasNotPassed(4) ? 0.005 : 0);

                if (ratio < max) {
                    if(vl++ > 3) {
                        flag(ratio + "<-" + max, true, true);
                    }
                } else vl = 0;

                debug("VL: " + vl + "RATIO: " + ratio + "/" + max + " VEL: " + MathUtils.round(velocityXZ, 5) + " DXZ" + MathUtils.round(move.getDeltaXZ(), 5) + " ONGROUND: " + getData().getMovementProcessor().isServerOnGround() + ", " + getData().getMovementProcessor().isClientOnGround());
                this.velocity = 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
