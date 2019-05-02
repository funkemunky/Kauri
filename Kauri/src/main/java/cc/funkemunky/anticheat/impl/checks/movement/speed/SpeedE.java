package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import lombok.var;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Speed (Type E)", description = "Totally not skidded off Lancer.", type = CheckType.SPEED)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class SpeedE extends Check {

    private int threshold;
    private double lastSpeed;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val player = getData().getPlayer();
        if (getData().isGeneralCancel()) {
            return;
        }

        val move = getData().getMovementProcessor();
        val to = move.getTo();
        val from = move.getFrom();

        val dx = to.getX() - from.getX();
        val dy = to.getY() - from.getY();
        val dz = to.getZ() - from.getZ();

        //@See net.minecraft.server.v1_8_R3.EntityLiving#L1238
        var f5 = 0.91f;

        val onGround = move.isClientOnGround();

        //noinspection
        @SuppressWarnings("UnusedAssignment")
        var moveSpeed = 0.0f;

        if (onGround) {
            //@See net.minecraft.server.v1_8_R3.EntityLiving#L1240
            f5 *= ReflectionsUtil.getFriction(BlockUtils.getBlock(move.getTo().toLocation(player.getWorld()).clone().subtract(0, 0.5f, 0)));
        }

        //@See net.minecraft.server.v1_8_R3.EntityLiving#L1243
        val f6 = 0.16277136F / (f5 * f5 * f5);

        //@See net.minecraft.server.v1_8_R3.EntityLiving#L1244
        if (onGround) {
            moveSpeed = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(player) * f6;

            //fixes a speed bug
            if (getData().getPlayer().isSprinting() && moveSpeed < 0.129) {
               moveSpeed *= 1.3;
            }

            //fixes momentum when you land
            if (dy > 0.0001) {
                moveSpeed += 0.2;
            }
        } else {
            moveSpeed = (getData().getActionProcessor().isSprinting() ? 0.026f : 0.02f) + 0.00001f;

            //fixes a speed bug
            if (getData().getActionProcessor().isSprinting() && moveSpeed < 0.026) {
                moveSpeed += 0.006;
            }

            if (dy < -0.08 && player.getFallDistance() == 0.0) {
                moveSpeed *= Math.abs(dy) * 1.3;
            }
        }

        val previousSpeed = this.lastSpeed;
        val speed = Math.sqrt(dx * dx + dz * dz);

        val speedChange = speed - previousSpeed;


        val delta = speedChange - moveSpeed;

        if (speed > 0.24 && delta > (move.isBlocksOnTop() ? 0.024 : 0.001) && getData().getVelocityProcessor().getLastVelocity().hasPassed(35)) {
            //To help prevent falses from things such as underblock/random running + jumping. Only falses once and it's rare and hard to get it to
            if ((threshold = Math.min(55, threshold + 2)) > 40) {
                flag(speedChange - moveSpeed + ">-0.001", true, true);
            }
        } else {
            threshold = Math.max(threshold - 1, 0);
        }

        debug("delta=" + delta + " vl=" + threshold);

        //@See net.minecraft.server.v1_8_R3.EntityLiving#L1285
        this.lastSpeed = speed * f5;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}