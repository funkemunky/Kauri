package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
//@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false)
public class VelocityB extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        Player player = getData().getPlayer();
        if (getData().getVelocityProcessor().getLastVelocity().hasPassed(2)) return;

        val dy = getData().getMovementProcessor().getTo().getY() - getData().getMovementProcessor().getFrom().getY();
        val dxz = Math.hypot(getData().getMovementProcessor().getTo().getX() - getData().getMovementProcessor().getFrom().getX(),
                getData().getMovementProcessor().getTo().getZ() - getData().getMovementProcessor().getFrom().getZ());

        val kbxz = Math.hypot(getData().getVelocityProcessor().getMotionX(), getData().getVelocityProcessor().getMotionZ());

        //the only accurate way to check horizontal kb is to check it in the air, if the player is on ground it won't work
        //people might say this is from agc or whatever but its from gcheat, just like entire agc is (no joke)
        if (getData().getMovementProcessor().getBlockAboveTicks() == 0
                && getData().getMovementProcessor().getLiquidTicks() == 0
                && getData().getMovementProcessor().getWebTicks() == 0
                && kbxz > 0.15
                && !getData().getMovementProcessor().isBlocksNear()) {

            val quotient = dxz / kbxz;

            val threshold = getData().getLastAttack().hasPassed(10) ? 0.6 : 0.5f;

            if (quotient < threshold) {
                if ((vl += 1.1) >= 8.0) {
                    flag("velocity: " + MathUtils.round(quotient * 100, 1) + "%", true, true);
                }
            } else {
                vl = Math.max(0, vl - 0.8);
            }

            debug("QUOTIENT: " + quotient + "/" + threshold + " VL: " + vl);
        }

        //debug("KBXZ: " + kbxz + " COLLIDE:" + noneCollide + " DXZ: " + dxz + " DY: " + dy + " KBY: " + getData().getVelocityProcessor().getMotionY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
