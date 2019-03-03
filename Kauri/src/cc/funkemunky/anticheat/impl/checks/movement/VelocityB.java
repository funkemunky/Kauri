package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class VelocityB extends Check {
    public VelocityB(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        Player player = getData().getPlayer();
        if(getData().getVelocityProcessor().getLastVelocity().hasPassed(2)) return;

        val dy = getData().getMovementProcessor().getTo().getY() - getData().getMovementProcessor().getFrom().getY();
        val dxz = cc.funkemunky.anticheat.api.utils.MiscUtils.hypot(getData().getMovementProcessor().getTo().getX() - getData().getMovementProcessor().getFrom().getX(),
                getData().getMovementProcessor().getTo().getZ() - getData().getMovementProcessor().getFrom().getZ());

        val kbxz = cc.funkemunky.anticheat.api.utils.MiscUtils.hypot(getData().getVelocityProcessor().getMotionX(), getData().getVelocityProcessor().getMotionZ());

        val noneCollide = getData().getBoundingBox().grow(1.5f, 0, 1.5f).getCollidingBlockBoxes(player).size() == 0;
        //the only accurate way to check horizontal kb is to check it in the air, if the player is on ground it won't work
        //people might say this is from agc or whatever but its from gcheat, just like entire agc is (no joke)
        if (getData().getMovementProcessor().getBlockAboveTicks() == 0
                && getData().getMovementProcessor().getLiquidTicks() == 0
                && getData().getMovementProcessor().getWebTicks() == 0
                && noneCollide) {

            val quotient = dxz / kbxz;

            if (quotient < 0.6) {
                if ((vl += 1.1) >= 15.0) {
                    flag("velocity: " + MathUtils.round(quotient * 100, 1) + "%", true, true);
                }
            } else {
                vl = Math.max(0, vl - 0.8);
            }

            debug("QUOTIENT: " + quotient + "/0.6" + " VL: " + vl);
        }

        //debug("KBXZ: " + kbxz + " COLLIDE:" + noneCollide + " DXZ: " + dxz + " DY: " + dy + " KBY: " + getData().getVelocityProcessor().getMotionY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
