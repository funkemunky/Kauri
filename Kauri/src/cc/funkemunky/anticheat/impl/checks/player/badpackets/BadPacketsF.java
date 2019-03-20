package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
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
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type F)", description = "Checks frequency of incoming packets. More reliable, but less detection.", type = CheckType.BADPACKETS, maxVL = 40)
public class BadPacketsF extends Check {

    @Setting(name = "threshold.time")
    private long threshold = 960L;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 4;

    private int ticks, vl;
    private long lastReset, lastTimeStamp;

    public BadPacketsF() {

    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!getData().isLagging() && timeStamp > lastTimeStamp + 5 && getData().getLastServerPos().hasPassed(2) && getData().getLastLogin().hasPassed(40)) {
            if (ticks++ >= 20) {
                val elapsed = timeStamp - lastReset;
                if (elapsed < threshold) {
                    if (vl++ > maxVL) {
                        flag(elapsed + "-<" + threshold, true, true);
                    }
                } else {
                    vl -= vl > 0 ? 1 : 0;
                }
                ticks = 0;
                lastReset = timeStamp;
            }
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}