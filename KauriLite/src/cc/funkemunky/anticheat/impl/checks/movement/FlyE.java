package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class FlyE extends Check {
    public FlyE(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.cancelForFlight(getData(), 15)) return;
        val move = getData().getMovementProcessor();
        val player = getData().getPlayer();
        val totalMaxY = 0.43 + PlayerUtils.getPotionEffectLevel(player, PotionEffectType.JUMP) * 0.12f;

        if(move.getDeltaY() > totalMaxY) {
            flag(move.getDeltaY() + ">-" + totalMaxY, true,true);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
