package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;
@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimJ extends Check {

    private double vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getPitchDelta() < 0.008 && move.getYawDelta() > 0.8) {
            if(vl++ > 8) {
                flag("pitchDelta=" + move.getPitchDelta(), true, true, vl > 15 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl-= vl > 0 ? 2 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
