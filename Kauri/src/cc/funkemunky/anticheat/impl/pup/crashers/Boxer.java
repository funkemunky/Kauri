package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.api.tinyprotocol.api.Packet;

@Packets(packets = {Packet.Client.HELD_ITEM_SLOT})
public class Boxer extends AntiPUP {

    private long lastTimeStamp;
    private int vl;

    public Boxer(String name, boolean enabled) {
        super(name, enabled);
    }

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        if(timeStamp - lastTimeStamp < 6) {
            if(vl++ > 3) {
                this.lastTimeStamp = timeStamp;
                return true;
            }
        } else vl = 0;
        this.lastTimeStamp = timeStamp;
        return false;
    }
}
