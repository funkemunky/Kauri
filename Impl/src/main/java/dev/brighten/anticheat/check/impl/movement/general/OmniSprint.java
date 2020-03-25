package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.util.Vector;

@CheckInfo(name = "OmniSprint", description = "Checks for sprinting in illegal directions.",
        checkType = CheckType.GENERAL, vlToFlag = 15, punishVL = 50, developer = true)
@Cancellable
public class OmniSprint extends Check {

    @Packet
    public void onMove(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && !data.playerInfo.generalCancel
                && data.playerInfo.liquidTimer.hasPassed(5)
                && !data.blockInfo.inWeb
                && data.playerInfo.lastTeleportTimer.hasPassed(5)
                && data.playerInfo.slimeTimer.hasPassed(10)
                && data.playerInfo.lastVelocity.hasPassed(10)) {
            val to = data.playerInfo.to.toVector();
            val from = data.playerInfo.from.toVector();
            Vector movement = new Vector(to.getX() - from.getX(), 0, to.getZ() - from.getZ()),
                    direction = new Vector(
                            -Math.sin(data.playerInfo.to.yaw * Math.PI / 180.0F) * 1 * 0.5, 0,
                            Math.cos(data.playerInfo.to.yaw * Math.PI  / 180) * 1 * 0.5);

            double delta = movement.distanceSquared(direction); //The distance between the player's actual velocity and what their velocity should be.

            if (delta > 0.22 //This is the delta if greater would be derived from walking on their direction's x axis or backwards.
                    && data.playerInfo.serverGround
                    && data.playerInfo.sprinting) {
                if (++vl > 7) {
                    flag(delta + ">-0.22");
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("vb=%v.1 move=%v dir=%v delta=%v.3", vl, movement.toString(), direction.toString(), delta);
        }
    }
}
