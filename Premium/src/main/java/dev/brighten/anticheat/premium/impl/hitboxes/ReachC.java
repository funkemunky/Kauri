package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Reach (C)", description = "Test reach check.", checkType = CheckType.HITBOX, developer = true)
public class ReachC extends Check {
    private EvictingList<KLocation> pastRelMoves = new EvictingList<>(20);
    private LivingEntity target;
    private boolean attacked;

    @Packet
    public void onRel(WrappedOutRelativePosition position, long now) {
        if(target == null) return;
        if(target.getEntityId() == position.getId()) {
            data.runKeepaliveAction(ka -> {
                float f = position.isLook() ? (float) (position.getYaw() * 360) / 256.0F : 0;
                float f1 = position.isLook() ? (float) (position.getPitch() * 360) / 256.0F : 0;
                KLocation loc = new KLocation(position.getX() / 32., position.getY() / 32., position.getZ() / 32.,
                        f, f1);
                loc.timeStamp = now;
                pastRelMoves.add(loc);
            });
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            if(target == null || target.getEntityId() != packet.getEntity().getEntityId()) pastRelMoves.clear();

            if(!(packet.getEntity() instanceof LivingEntity)) {
                target = null;
                pastRelMoves.clear();
                return;
            }

            target = (LivingEntity) packet.getEntity();
            attacked = true;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if (attacked) {

            KLocation entityLoc = new KLocation(target.getLocation()), previous = new KLocation(0,0, 0);

            int ping = MathUtils.floor(data.lagInfo.transPing / 50.);

            for (int i = pastRelMoves.size() - 1; i > 0; i--) {
                val move = pastRelMoves.get(i);

                entityLoc.x-= move.x;
                entityLoc.y-= move.y;
                entityLoc.z-= move.z;

                debug("(%v) x=%v.4 y=%v.4 z=%v.4", i, entityLoc.x, entityLoc.y, entityLoc.z);
            }
            int count = 0;

            long locDif = now - entityLoc.timeStamp;

            double pct = (locDif - data.lagInfo.transPing)
                    / (double) (locDif - (now - previous.timeStamp));
            attacked = false;
        }
    }
}
