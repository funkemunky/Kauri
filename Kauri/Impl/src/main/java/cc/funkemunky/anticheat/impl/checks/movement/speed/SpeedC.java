package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.PlayerUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@CheckInfo(name = "Speed (Type C)", description = "Checks for legitimate acceleration on ground.", type = CheckType.SPEED, maxVL = 75)
public class SpeedC extends Check {

    private int vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getBlockBelow() == null) return;

        if(move.getGroundTicks() > 2 && !getData().isGeneralCancel() && move.getHalfBlockTicks() == 0 && move.getBlockAboveTicks() == 0 && move.getWebTicks() == 0 && move.getClimbTicks() == 0 && move.getLiquidTicks() == 0) {
            double predicted = move.getLastDeltaXZ() * ReflectionsUtil.getFriction(getData().getBlockBelow());
            double delta = Math.abs((move.getDeltaXZ() - predicted) * 8.7 - (0.5 * PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED)));

            if(delta > 1.1 && getData().getWalkSpeed() < 0.21) {
                if(vl++ > 5) {
                    flag("delta=" + delta, true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 2 : 0;
            debug("delta=" + delta + " vl=" + vl);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
