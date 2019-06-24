package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;

@Packets(packets = {Packet.Client.HELD_ITEM_SLOT})
public class Boxer extends AntiPUP {

    private long lastTimeStamp;
    private int vl;

    public Boxer(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        if (timeStamp - lastTimeStamp < 6) {
            if (vl++ > 3) {
                this.lastTimeStamp = timeStamp;
                return true;
            }
        } else vl = 0;
        this.lastTimeStamp = timeStamp;
        return false;
    }
}
