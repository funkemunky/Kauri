package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LOOK})
public class BadPacketsH extends Check {
    public BadPacketsH(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Setting(name = "threshold.vl.max")
    private int vlThreshold = 10;

    private int vl = 0;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getActionProcessor().isOpenInventory()) {
            val move = getData().getMovementProcessor();
            if(!packetType.contains("Position") || (!move.isInLiquid() && move.isServerOnGround() && !move.isOnClimbable())) {
                if(vl++ > vlThreshold) {
                    flag(vl + ">-" + vlThreshold, true, true);
                }
            } else vl = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
