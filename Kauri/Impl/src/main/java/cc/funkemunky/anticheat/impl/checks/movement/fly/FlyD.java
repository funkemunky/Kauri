package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Fly (Type D)", description = "Ensures the jump height of the player is legitimate.", type = CheckType.FLY, maxVL = 15)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyD extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float predicted = MiscUtils.getPredictedJumpHeight(getData());
        if(move.isHasJumped()
                && move.getDeltaY() != predicted
                && !move.isServerPos()
                && !getData().isLagging()
                && Kauri.getInstance().getTps() > 16
                && move.getAirTicks() == 1
                && move.getBlockAboveTicks() == 0
                && move.getHalfBlockTicks() == 0
                && move.getDeltaY() > 0
                && !move.isOnSlimeBefore()
                && move.getLiquidTicks() == 0
                && move.getWebTicks() == 0
                && move.getClimbTicks() == 0
                && getData().getVelocityProcessor().getLastVelocity().hasPassed(5 + MathUtils.millisToTicks(getData().getTransPing()))) {
            if(verbose.flag(2, 2000L)) {
                flag("predicted=" + predicted + " deltaY=" + move.getDeltaY(), true, true, AlertTier.HIGH);
            }
        }

        debug("predicted=" + predicted + " deltaY=" + move.getDeltaY() + " onGround=" + move.isServerOnGround() + " airTicks=" + move.getAirTicks());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
