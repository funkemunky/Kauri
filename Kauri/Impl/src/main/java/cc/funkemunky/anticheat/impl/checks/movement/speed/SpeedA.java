package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Speed (Type A)", type = CheckType.SPEED, description = "A general limiting speed check with a verbose to prevent false positives.", maxVL = 40, executable = true)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class SpeedA extends Check {

    private float vl = 0;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().isGeneralCancel()) return;
        val move = getData().getMovementProcessor();
        float threshold = MiscUtils.getBaseSpeed(getData()) + (move.isServerOnGround() ? (move.getGroundTicks() > 8 ? 0.05f : 0.06f) : 0.1f);

        threshold+= move.getHalfBlockTicks() > 0 ? 0.08 : 0;
        threshold+= move.isOnSlimeBefore() ? 0.025 : 0;
        threshold+= move.getBlockAboveTicks() > 0 ? (move.getIceTicks() > 0 ? 0.565f : 0.25f) : 0;
        threshold+= move.getIceTicks() > 0 && (move.getGroundTicks() < 8 || move.getAirTicks() < 3) ? 0.28 : 0;

        if(move.getDeltaXZ() > threshold && getData().getVelocityProcessor().getLastVelocity().hasPassed(60)) {
            if(Math.min(vl++, 40) > 20) {
                flag("speed=" + move.getDeltaXZ(), true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;

        debug(move.getDeltaXZ() + ">-" + threshold + " vl=" + vl + " iceTicks=" + move.getIceTicks() + " block=" + move.getBlockAboveTicks() + " half=" + move.getHalfBlockTicks() + " serverPos=" + (System.currentTimeMillis() - getData().getLastServerPosStamp()));
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
