package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import cc.funkemunky.api.tinyprotocol.packet.types.Vec3D;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Reach (C)", checkType = CheckType.HITBOX, punishVL = 2)
public class ReachC extends Check {

    private long lastUse;
    private double buffer;
    private Entity entity;

    @Packet
    public void onFly(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastUse < 1 && entity != null && entity instanceof Player) {

            if(data.playerInfo.creative) return;

            List<KLocation> originLocs = Stream.of(data.playerInfo.to.clone(), data.playerInfo.from.clone())
                    .peek(loc -> loc.y +=  data.playerInfo.sneaking ? 1.54 : 1.62)
                    .collect(Collectors.toList());

            List<BoundingBox> entityLocs = data.targetPastLocation.getEstimatedLocation(timeStamp,
                    data.lagInfo.transPing,
                    200L + Math.abs(data.lagInfo.transPing - data.lagInfo.lastTransPing))
                    .stream()
                    .map(ReachC::getHitbox)
                    .collect(Collectors.toList());

            double distance = 69;
            int misses = 0, collided = 0;
            for (KLocation originLoc : originLocs) {
                for (BoundingBox box : entityLocs) {
                    val dir = MathUtils.getDirection(originLoc);
                    val eyePoint = new Vector(originLoc.x, originLoc.y, originLoc.z);
                    val reach = new Vector(dir.getX() * 7, dir.getY() * 7, dir.getZ() * 7);
                    val check = MiscUtils.calculateIntercept(box, eyePoint,
                            eyePoint.clone().add(new Vector(originLoc.x * reach.getX(), originLoc.y * reach.getY(), originLoc.z * reach.getZ())));

                    if(check != null) {
                        val newdist = new Vector(check.a, check.b, check.c).distance(originLoc.toVector());
                        distance = Math.min(distance, newdist);
                        collided++;
                    } else misses++;
                }
            }

            if(distance == 69) {
                buffer-= buffer > 0 ? 0.001 : 0;
                return;
            }

            if(collided > 2 && data.lagInfo.lastPacketDrop.hasPassed(2)) {
                if(distance > 3.03 &&
                        Kauri.INSTANCE.lastTickLag.hasPassed(40)) {
                    if(++buffer > 2) {
                        vl++;
                        flag("distance=%v.3 buffer=%v.1 misses=%v", distance, buffer, misses);
                    }
                } else buffer-= buffer > 0 ? 0.01 : 0;
            }

            debug("distance=%v.3 buffer=%v.1 ticklag=%v collided=%v",
                    distance, buffer, Kauri.INSTANCE.lastTickLag.getPassed(), collided);
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        lastUse = timeStamp;
        entity = packet.getEntity();
    }

    @Packet
    public void onArm9(WrappedInArmAnimationPacket packet) {
        buffer-= buffer > 0 ? 0.001 : 0;
    }

    private static BoundingBox getHitbox(KLocation loc) {
        return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(0.4f, 0.1f, 0.4f)
                .expandMax(0,1.8,0).toBoundingBox();
    }
}
