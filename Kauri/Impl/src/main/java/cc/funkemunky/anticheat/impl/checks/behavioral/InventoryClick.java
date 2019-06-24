package cc.funkemunky.anticheat.impl.checks.behavioral;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.RollingAverage;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "InventoryClick", description = "Looks for consistent cps inside an inventory and out.", type = CheckType.BEHAVIORAL, cancelType = CancelType.NONE, cancellable = false, executable = false)
@Packets(packets = {Packet.Client.ARM_ANIMATION, Packet.Client.WINDOW_CLICK})
public class InventoryClick extends Check {

    private RollingAverage inventoryCPS = new RollingAverage(40), armCPS = new RollingAverage(40);
    private long lastInv, lastArm;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.ARM_ANIMATION)) {
            if(!MiscUtils.shouldReturnArmAnimation(getData())) {
                val armDelta = timeStamp - lastArm;
                val cps = 1000D / armDelta;

                if(armDelta < 350 && !Double.isInfinite(cps)) {
                    armCPS.add(cps, timeStamp);
                }

            }
            lastArm = timeStamp;
        } else {
            val invDelta = timeStamp - lastInv;
            val cps = 1000D / invDelta;

            if(invDelta < 350 && !Double.isInfinite(cps)) {
                inventoryCPS.add(cps, timeStamp);

                double armAverage = armCPS.getAverage(), invAverage = inventoryCPS.getAverage();

                if(armAverage > 8 && invAverage > 8) {
                    double delta = MathUtils.getDelta(armAverage, invAverage);

                    if(delta < 1) {
                        if(vl++ > 5) {
                            flag("arm=" + MathUtils.round(armAverage, 2) + " inv=" + MathUtils.round(invAverage, 2), true, true, vl > 12 ? AlertTier.LIKELY : vl > 8 ? AlertTier.POSSIBLE : AlertTier.LOW);
                        }
                    } else vl = 0;

                    debug("delta=" + (float) delta + " vl=" + vl);
                }

                debug("arm=" + MathUtils.round(armAverage, 2) + " inv=" + MathUtils.round(invAverage, 2));
            }
            lastInv = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
