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
@CheckInfo(name = "NoSlowdown (Type A)", description = "Looks for players using items but not slowing down. (Players can do this with a glitch, so do not ban).", executable = false, developer = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class NoSlowdownA extends Check {

    private Verbose verbose = new Verbose();
    private long lastTimeStamp;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val action = getData().getActionProcessor();
        val baseSpeed = MiscUtils.getBaseSpeed(getData()) - 0.02f;

        if(timeStamp - lastTimeStamp < 5 || getData().isGeneralCancel()) return;

        if(action.isUsingItem() && move.getDeltaXZ() > baseSpeed) {
            if(verbose.flag(20, 500L)) {
                flag(MathUtils.round(move.getDeltaXZ(), 3) + ">- " + baseSpeed, true,false);
            }
            debug(verbose.getVerbose() + ": " + move.getDeltaXZ() + ">-" + baseSpeed);
        } else verbose.deduct();
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
