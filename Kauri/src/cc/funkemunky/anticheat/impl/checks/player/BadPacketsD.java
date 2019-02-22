package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.BLOCK_PLACE,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class BadPacketsD extends Check {
    public BadPacketsD(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private long lastFlying;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.BLOCK_PLACE)) {
            val elapsed = timeStamp - lastFlying;
            if(elapsed < 10) {
                if(vl++ > 5) {
                    flag(elapsed + "<-10", true, true);
                }
            } else {
                vl-= vl > 0 ? 1 : 0;
            }
        } else {
            lastFlying = timeStamp;
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
