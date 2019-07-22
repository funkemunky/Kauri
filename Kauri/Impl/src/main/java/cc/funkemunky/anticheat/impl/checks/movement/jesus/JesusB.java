package cc.funkemunky.anticheat.impl.checks.movement.jesus;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Init
@CheckInfo(name = "Jesus (Type B)", description = "Makes sure the player isn't going faster than a certain speed in water.", type = CheckType.JESUS, maxVL = 60, enabled = false, executable = false)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class JesusB extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if((move.isInLiquid() || move.isLiquidBelow()) && !getData().isGeneralCancel()) {
            float threshold = move.getLiquidTicks() > 15 ? 0.198f : 0.38f;

            threshold+= PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED) * 0.015;
            int depthStrider = PlayerUtils.getDepthStriderLevel(getData().getPlayer());
            float depthMult = Math.min(3, depthStrider);
            if(depthStrider > 0) {
                if(move.isServerOnGround()) depthMult*= 0.5f;

                depthMult = (float) Math.sqrt((Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer()) * 1 - 0.02f) * depthMult / 3);
            }

            threshold+= depthMult;

            if(move.getDeltaXZ() > threshold) {
                if(vl++ > 5 || move.getDeltaXZ() - threshold > 0.3) {
                    flag(move.getDeltaXZ() + ">-" + threshold, true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
