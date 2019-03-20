package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class ArmSwing extends AntiPUP {
    public ArmSwing(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private long lastTimeStamp;
    private int vl;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        if (timeStamp - lastTimeStamp < 6) {
            if (vl++ > 6) {
                lastTimeStamp = timeStamp;
                return true;
            }
        } else vl = 0;
        lastTimeStamp = timeStamp;
        return false;
    }
}
