package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

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
        if(!item.getType().isSolid() && (BlockUtils.isTool(item) || item.getType().isEdible())) {
            usingItem = true;
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            usingItem = false;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            val base = MovementUtils.getBaseSpeed(data) - 0.082f;

            if(usingItem
                    && data.playerInfo.deltaXZ > base
                    && data.playerInfo.serverGround
                    && data.playerInfo.lastVelocity.hasPassed(20)
                    && !data.playerInfo.generalCancel) {
                if(vl++ > 5) {
                    flag("deltaXZ=" + data.playerInfo.deltaXZ + " base=" + base);
                }
            } else vl-= vl > 0 ? 0.25f : 0;

            debug("using=" + usingItem + " xz=" + data.playerInfo.deltaXZ + " base=" + base);
        }
    }
}
