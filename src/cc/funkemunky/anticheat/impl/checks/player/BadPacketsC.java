package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
public class BadPacketsC extends Check {
    public BadPacketsC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

        val pitch = Math.abs(flying.getPitch());

        if(pitch > 90) {
            flag(pitch + ">-90", true, true);
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
