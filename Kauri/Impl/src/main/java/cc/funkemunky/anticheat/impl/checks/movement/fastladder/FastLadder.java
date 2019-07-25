package cc.funkemunky.anticheat.impl.checks.movement.fastladder;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@cc.funkemunky.api.utils.Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@CheckInfo(name = "FastLadder", description = "Looks for any suspicious vertical speed values while climbing.", type = CheckType.MOVEMENT, maxVL = 40)
public class FastLadder extends Check {

    @Setting(name = "threshold.verboseMaxSpeed")
    private float verboseMaxSpeed = 0.124f;

    @Setting(name = "threshold.maxSpeed")
    private float maxSpeed = 0.45f;

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(!getData().isGeneralCancel() && getData().getBlockInside() != null && BlockUtils.isClimbableBlock(getData().getBlockInside()) && move.isOnClimbable()) {
            float max = verboseMaxSpeed + PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.JUMP) * 0.1f;
            if (move.getDeltaY() > max) {
                if (vl++ > 7) {
                    flag(move.getDeltaY() + ">-" + verboseMaxSpeed, true, true, AlertTier.HIGH);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }

            debug("vl=" + vl + " deltaY=" + move.getDeltaY() + ">-" + max);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
