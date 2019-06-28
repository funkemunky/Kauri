package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type B)", description = "Looks for any invalid movements while a player has an inventory window open.", type = CheckType.BADPACKETS, maxVL = 80, executable = false, developer = true)
public class BadPacketsB extends Check {

    @Setting(name = "threshold.vl.max")
    private int vlThreshold = 10;

    @Setting(name = "closeInventory")
    private boolean closeInventory = true;

    private int vl = 0;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getActionProcessor().isOpenInventory()) {
            val move = getData().getMovementProcessor();
            if (!packetType.contains("Position") || (!move.isNearLiquid() && move.isServerOnGround() && !move.isOnClimbable())) {
                if (getData().getMovementProcessor().getDeltaXZ() > 0.1 && vl++ > vlThreshold) {
                    flag(vl + ">-" + vlThreshold, true, true, AlertTier.POSSIBLE);
                    if(closeInventory) {
                        getData().getPlayer().closeInventory();
                    }
                }
            } else vl = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
