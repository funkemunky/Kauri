package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.event.player.PlayerMoveEvent;

@CheckInfo(name = "NoSlowdown",
        description = "Checks for a player not slowing down when using an action.", checkType = CheckType.GENERAL,
        punishVL = 30, developer = true)
public class NoSlowdown extends Check {

    private boolean usingItem;

    @Packet
    public void onDig(WrappedInBlockDigPacket packet) {
        usingItem = false;
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        if(packet.getPlayer().getItemInHand() == null) return;
        val item = packet.getPlayer().getItemInHand();
        if(!item.getType().isSolid() && (item.getType().name().contains("SWORD"))) {
            usingItem = true;
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            usingItem = false;
        }
    }

    @Event
    public void onMove(PlayerMoveEvent event) {
        if(event.getFrom().distance(event.getTo()) > 0) {
            val base = MovementUtils.getBaseSpeed(data) - 0.082f;

            val deltaXZ = MathUtils.getHorizontalDistance(event.getFrom(), event.getTo());
            if(usingItem
                    && deltaXZ > base
                    && event.getPlayer().isOnGround()
                    && data.playerInfo.lastVelocity.hasPassed(20)
                    && !data.playerInfo.generalCancel) {
                if(vl++ > 5) {
                    if(vl > 12) {
                        flag("deltaXZ=" + data.playerInfo.deltaXZ + " base=" + base);
                    }
                    event.setCancelled(true);
                }
            } else vl-= vl > 0 ? 0.25f : 0;

            debug("using=" + usingItem + " xz=" + data.playerInfo.deltaXZ + " base=" + base);
        }
    }
}
