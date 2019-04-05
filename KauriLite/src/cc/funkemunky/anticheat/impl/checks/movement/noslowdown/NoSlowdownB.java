package cc.funkemunky.anticheat.impl.checks.movement.noslowdown;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "NoSlowdown (Type B)", description = "Looks for players not slowing down on soul sand.", executable = false, developer = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class NoSlowdownB extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val baseSpeed = MiscUtils.getBaseSpeed(getData()) - 0.04;

        if(getData().isGeneralCancel()) return;

        if(move.isOnSoulSand() && move.isServerOnGround() && move.getDeltaXZ() > baseSpeed) {
            if(verbose.flag(6, 400L)) {
                flag(MathUtils.round(move.getDeltaXZ(), 3) + ">-" + baseSpeed, true, false);
            }
        } else verbose.deduct();
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
