package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class BadPacketsF extends Check {

    @Setting(name = "threshold")
    private long threshold = 960L;

    private int ticks, vl;
    private long lastReset;
    public BadPacketsF(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!getData().isLagging() && getData().getLastServerPos().hasPassed(2)  && getData().getLastLogin().hasPassed(40)) {
           if(ticks++ >= 20) {
               val elapsed = timeStamp - lastReset;
               if(elapsed < threshold) {
                   if(vl++ > 3) {
                       flag(elapsed + "-<" + threshold, true, true);
                   }
               } else {
                   vl -= vl > 0 ? 1 : 0;
               }
               ticks = 0;
               lastReset = timeStamp;
           }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}