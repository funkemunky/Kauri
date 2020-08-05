package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import com.google.gson.internal.$Gson$Preconditions;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Reach (C)", description = "Test reach check.", checkType = CheckType.HITBOX, developer = true)
public class ReachC extends Check {
    private KLocation current, previous, newLoc, incremented, lastCorrectOne;
    private List<KLocation> locs = new ArrayList<>();
    private int inc, xLoc, yLoc, zLoc;
    private LivingEntity target;
    private boolean attacked;
    @Packet
    public void onTrans(WrappedOutTransaction packet, long now) {
        if(packet.getId() == 0 && target != null) {

            EntityLiving entity = ((CraftLivingEntity)target).getHandle();
            val tracker = ((WorldServer)entity.getWorld()).tracker.trackedEntities.get(target.getEntityId());

            if(tracker != null) {
                final int xLoc = tracker.xLoc, yLoc = tracker.yLoc, zLoc = tracker.zLoc;
                if(current != null) {
                    data.runKeepaliveAction(ka -> {
                        if(xLoc != (int)newLoc.x || yLoc != (int)newLoc.y || zLoc != (int)newLoc.z) {
                            previous = newLoc;
                            this.xLoc = xLoc;
                            this.yLoc = yLoc;
                            this.zLoc = zLoc;
                            newLoc = new KLocation(xLoc, yLoc, zLoc);
                            inc = 3;
                            locs.clear();
                        }
                        //this.posX + (this.newPosX - this.posX) / (double) this.newPosRotationIncrements;
                        if (inc > 0) {

                            double x = current.x + (newLoc.x / 32. - current.x) / inc;
                            double y = current.y + (newLoc.y / 32. - current.y) / inc;
                            double z = current.z + (newLoc.z / 32. - current.z) / inc;

                            current = incremented = new KLocation(x, y, z);
                            current.timeStamp = now;

                            locs.add(current);

                            //debug("%v x=%v.4 y=%v.4 z=%v.4", inc, incremented.x, incremented.y, incremented.z);

                            inc--;
                        }
                    });
                } else {
                    newLoc = current = previous = new KLocation(tracker.xLoc, tracker.yLoc, tracker.zLoc);
                    current.x/=32.;
                    current.y/=32.;
                    current.z/=32.;
                }
            } else {
                if(current != null) {
                    data.runKeepaliveAction(ka -> {
                        if(xLoc != (int)newLoc.x || yLoc != (int)newLoc.y || zLoc != (int)newLoc.z) {
                            previous = newLoc;
                            newLoc = new KLocation(xLoc, yLoc, zLoc);
                            inc = 3;
                            locs.clear();
                        }
                        //this.posX + (this.newPosX - this.posX) / (double) this.newPosRotationIncrements;
                        if (inc > 0) {

                            double x = current.x + (newLoc.x / 32. - current.x) / inc;
                            double y = current.y + (newLoc.y / 32. - current.y) / inc;
                            double z = current.z + (newLoc.z / 32. - current.z) / inc;

                            previous = current;
                            current = incremented = new KLocation(x, y, z);
                            current.timeStamp = now;

                            locs.add(current);
                            //debug("%v x=%v.4 y=%v.4 z=%v.4", inc, incremented.x, incremented.y, incremented.z);

                            inc--;
                        }
                    });
                } else {
                    newLoc = current = previous = new KLocation(tracker.xLoc, tracker.yLoc, tracker.zLoc);
                    current.x/=32.;
                    current.y/=32.;
                    current.z/=32.;
                }
            }
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            if(target == null || target.getEntityId() != packet.getEntity().getEntityId()) current = previous = null;

            if(!(packet.getEntity() instanceof LivingEntity)) {
                target = null;
                current = previous = null;
            }

            target = (LivingEntity) packet.getEntity();

            attacked = true;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(attacked) {
            if(current != null && previous != null && incremented != null) {
                List<Location> origins = new ArrayList<>();

                val from = data.playerInfo.from.clone();
                val to = data.playerInfo.to.clone();
                from.y+= data.playerInfo.sneaking ? 1.54 : 1.62;
                to.y+= data.playerInfo.sneaking ? 1.54: 1.62;

                origins.add(from.toLocation(packet.getPlayer().getWorld()));
                origins.add(to.toLocation(packet.getPlayer().getWorld()));

                List<SimpleCollisionBox> boxes = new ArrayList<>();

                KLocation current = previous.clone();

                for (KLocation loc : locs) {
                    boxes.add(((SimpleCollisionBox)EntityData.getEntityBox(loc, target)).expand(0.1));
                }

                double distance = 69;

                for (Location origin : origins) {
                    Vec3D vec = new Vec3D(origin.getX(), origin.getY(), origin.getZ());
                    val dirVec = origin.clone().add(origin.getDirection().multiply(10));
                    Vec3D dir = new Vec3D(dirVec.getX(), dirVec.getY(), dirVec.getZ());
                    for (SimpleCollisionBox box : boxes) {
                        AxisAlignedBB aabb = box.toAxisAlignedBB();
                        MovingObjectPosition move = aabb.a(vec, dir);

                        if(move != null) {
                            distance = Math.min(distance, Math.sqrt(move.pos.distanceSquared(vec)));
                        }
                    }
                }

                if(distance == 69) return;

                if(distance > 3) {
                    vl++;
                    flag("dist=%v", distance);
                } else if(vl > 0) vl-= 0.1f;

                debug("dist=%v vl=%v.2", distance, vl);
                debug("(TO) x=%v.4 y=%v.4 z=%v.4",
                        previous.x, previous.y, previous.z);
                debug("(FROM) x=%v.4 y=%v.4 z=%v.4",
                        current.x, current.y, current.z);
                debug("(INC) x=%v.4 y=%v.4 z=%v.4",
                        incremented.x, incremented.y, incremented.z);
                lastCorrectOne = incremented;
            }
            attacked = false;
        }
    }
}
