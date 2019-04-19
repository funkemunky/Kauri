package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class YLevel extends AntiPUP {
    public YLevel(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

        return Math.abs(flying.getY()) > 300;
    }
}
