package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@CheckInfo(name = "Reach (B)", punishVL = 6, executable = true,
        checkType = CheckType.HITBOX)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {
    private float buffer;
    private int hbuffer;

    public Timer lastAimOnTarget = new TickTimer();
    private final Queue<Entity> attacks = new LinkedBlockingQueue<>();

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
            EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                && allowedEntityTypes.contains(packet.getEntity().getType())) {
            attacks.add(packet.getEntity());
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.creative || data.playerInfo.inVehicle) {
            attacks.clear();
            debug("creative or in vehicle");
           return;
        }
        Entity target;

        while((target = attacks.poll()) != null) {
            //Updating new entity loc
            Optional<EntityLocation> optionalEloc = data.entityLocationProcessor.getEntityLocation(target);

            if(!optionalEloc.isPresent()) {
                debug("eloc is null");
                return;
            }

            final EntityLocation eloc = optionalEloc.get();

            final KLocation to = data.playerInfo.to.clone(), from = data.playerInfo.from.clone();

            //debug("current loc: %.4f, %.4f, %.4f", eloc.x, eloc.y, eloc.z);

            to.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;
            from.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;

            if(eloc.x == 0 && eloc.y == 0 & eloc.z == 0) {
                debug("eloc is all 0 wtf");
                return;
            }

            double distance = Double.MAX_VALUE;
            boolean collided = false; //Using this to compare smaller numbers than Double.MAX_VALUE. Slightly faster

            List<SimpleCollisionBox> boxes = new ArrayList<>();
            if(eloc.oldLocations.size() > 0) {
                for (KLocation oldLocation : eloc.oldLocations) {
                    SimpleCollisionBox box = (SimpleCollisionBox)
                            EntityData.getEntityBox(oldLocation.toVector(), target);

                    if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                        box = box.expand(0.1);
                    } else box = box.expand(0.0325);
                    boxes.add(box);
                }
                for (KLocation oldLocation : eloc.interpolatedLocations) {
                    SimpleCollisionBox box = (SimpleCollisionBox)
                            EntityData.getEntityBox(oldLocation.toVector(), target);

                    if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                        box = box.expand(0.1);
                    } else box = box.expand(0.0325);
                    boxes.add(box);
                }
            } else {
                for (KLocation oldLocation : eloc.interpolatedLocations) {
                    SimpleCollisionBox box = (SimpleCollisionBox)
                            EntityData.getEntityBox(oldLocation.toVector(), target);

                    if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                        box = box.expand(0.1);
                    } else box = box.expand(0.0325);
                    boxes.add(box);
                }
            }

            if(boxes.size() == 0) return;

            int hits = 0;

            for (SimpleCollisionBox targetBox : boxes) {
                final AxisAlignedBB vanillaBox = new AxisAlignedBB(targetBox);

                Vec3D intersectTo = vanillaBox.rayTrace(to.toVector(), MathUtils.getDirection(to), 10),
                        intersectFrom = vanillaBox.rayTrace(from.toVector(),
                                MathUtils.getDirection(from), 10);

                if(intersectTo != null) {
                    lastAimOnTarget.reset();
                    hits++;
                    distance = Math.min(distance, intersectTo.distanceSquared(new Vec3D(to.x, to.y, to.z)));
                    collided = true;
                }
                if(intersectFrom != null) {
                    lastAimOnTarget.reset();
                    hits++;
                    distance = Math.min(distance, intersectFrom.distanceSquared(new Vec3D(to.x, to.y, to.z)));
                    collided = true;
                }
            }

            if(collided) {
                hbuffer = 0;
                distance = Math.sqrt(distance);
                if(distance > 3.05) {
                    if(++buffer > 2) {
                        vl++;
                        flag("d=%.3f>-3.05", distance);
                        buffer = Math.min(2, buffer);
                    }
                } else if(buffer > 0) buffer-= 0.05f;
                debug((distance > 3.001 ? Color.Green : "")
                                +"dist=%.2f>-3.001 hits-%s b=%s ld=%s",
                        distance, hits, buffer,
                        data.lagInfo.lastPingDrop.getPassed());
            } else {
                if (++hbuffer > 5) {
                    find(HitboxesB.class).vl++;
                    find(HitboxesB.class).flag(120, "%.1f;%.1f;%.1f", eloc.x, eloc.y, eloc.z);
                }
                debug("didnt hit box: x=%.1f y=%.1f z=%.1f", eloc.x, eloc.y, eloc.z);
            }
        }
    }

}
