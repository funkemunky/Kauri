package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

import java.util.UUID;

@CheckInfo(name = "Killaura (Type D)", description = "Looks for extremely fast switching between targets.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, maxVL = 35, executable = true)
@Init
@Packets(packets = {Packet.Client.USE_ENTITY})
public class KillauraD extends Check {

    private UUID lastTarget;
    private long lastSwitch;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getTarget() == null || getData().isLagging() || Kauri.getInstance().getTps() < 15) return;

        long delta = timeStamp - lastSwitch;

        debug("delta=" + delta);

        if(delta < 50) {
            if(vl++ > 12) {
                flag(delta + "<-50", true, true, vl > 30 ? AlertTier.CERTAIN : AlertTier.HIGH);
            }
        } else vl = 0;

        if(!getData().getTarget().getUniqueId().equals(lastTarget)) {
            lastSwitch = timeStamp;
        }
        lastTarget = getData().getTarget().getUniqueId();
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
