package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Fly (Type F)", description = "Ensures the player's acceleration is what it should be.", type = CheckType.FLY, maxVL = 50)
//@Init
@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyF extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val accel = Math.abs(move.getClientYAcceleration());

        if(MiscUtils.cancelForFlight(getData(), 4, false)) return;

        if(accel < 0.065 && move.getAirTicks() > 3) {
            if(vl++ > 3) {
                flag("accel=" + accel, true, true);
            }
        } else vl-= vl > 0 ? 1 : 0;

        debug("vl=" + vl + " accel=" + accel);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
