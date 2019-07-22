package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "GroundSpoof (Type B)", description = "Ensures the fall distance is what it should be.", type = CheckType.MOVEMENT, executable = false)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@Init
public class GroundSpoofB extends Check {

    private float lastFallDistance = 0;
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().isGeneralCancel() || getData().getMovementProcessor().getDeltaY() == 0) {
            lastFallDistance = 0;
            return;
        }

        val fallDistance = getData().getPlayer().getFallDistance();
        val deltaFD = MathUtils.getDelta(fallDistance, lastFallDistance);
        val move = getData().getMovementProcessor();

        if(move.getDeltaY() < 0 && MathUtils.getDelta(deltaFD, Math.abs(move.getDeltaY())) > Math.max(0.5, fallDistance / 5)) {
            if(verbose.flag(12, 500L)) {
                flag(deltaFD + "<-" + move.getDeltaY(), true, true, verbose.getVerbose() > 10 ? AlertTier.LIKELY : AlertTier.POSSIBLE);
            }
        } else verbose.deduct();
        lastFallDistance = fallDistance;
        debug(getData().getPlayer().getFallDistance() + "");
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
