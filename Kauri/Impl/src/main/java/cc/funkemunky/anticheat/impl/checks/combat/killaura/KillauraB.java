package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.USE_ENTITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Killaura (Type B)", description = "Checks for clients sprinting while attacking.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, maxVL = 60)
public class KillauraB extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if (use.getEntity() instanceof Player && use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) { //A player only stops sprinting when hitting a player.
            val move = getData().getMovementProcessor();

            if (!getData().isGeneralCancel() && !getData().isLagging() && !getData().takingVelocity(5) && (move.getDeltaXZ() > move.getBaseSpeed() && use.getPlayer().isSprinting() && getData().getActionProcessor().isSprinting())) {
                if (verbose.flag(15, 800L)) { //We add a verbose or redundancy.
                    flag(move.getDeltaXZ() + ">-" + move.getBaseSpeed(), true, true, AlertTier.LIKELY);
                }
            } else verbose.deduct();
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
