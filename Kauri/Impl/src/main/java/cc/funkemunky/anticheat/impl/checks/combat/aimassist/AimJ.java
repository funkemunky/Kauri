package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.FLYING})
public class AimJ extends Check {

    private Interval<Double> interval = new Interval<>(0, 20);
    private long lastFlying;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

        debug(packetType + ": " + (timeStamp - lastFlying));

        lastFlying = timeStamp;
        //val move = getData().getMovementProcessor();
        //debug(packetType + ": " + move.getYawDelta() + ", " + move.getPitchDelta() + ", " + move.getDeltaXZ());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
