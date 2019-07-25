package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
@CheckInfo(name = "AutoClicker (Type I)", type = CheckType.AUTOCLICKER, maxVL = 50, executable = false)
public class AutoClickerI extends Check {

    private int vl;
    private double lastRange;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val swing = getData().getSwingProcessor();

        if(!MiscUtils.shouldReturnArmAnimation(getData()) && (timeStamp - swing.getLastDequeueProcess()) < 6) {
            double range = MathUtils.getDelta(swing.getMax(), swing.getMin()), delta = MathUtils.getDelta(range, lastRange);
            if(range > 149 && delta <= 5) {
                if(vl++ > 2) {
                    flag("distinct=" + swing.getDistinct() + " stdDelta=" + swing.getStdDelta(), true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug( "range=" + range + " delta=" + delta + " vl=" + vl + " lastBlockBreak" + getData().getLastBlockBreak().getPassed() + " isBreaking=" + getData().isBreakingBlock());
            lastRange = range;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
