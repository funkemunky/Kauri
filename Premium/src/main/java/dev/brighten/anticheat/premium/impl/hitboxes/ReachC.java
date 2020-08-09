package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.*;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import net.minecraft.server.v1_8_R3.MathHelper;
import org.bukkit.entity.LivingEntity;

@CheckInfo(name = "Reach (C)", description = "Test reach check.", checkType = CheckType.HITBOX, developer = true)
public class ReachC extends Check {
    private LivingEntity target;
    private boolean attacked;
    private KLocation lastLoc = new KLocation(0,0,0), current;
    private EvictingList<KLocation> locations = new EvictingList<>(30);

    @Packet
    public boolean onIn(WrappedOutRelativePosition packet, long now) {
        //this.posX + (this.newPosX - this.posX) / (double) this.newPosRotationIncrements;
        if(target == null) return false;

        lastLoc = current;
        current = new KLocation(target.getLocation());
        current.x = deaccurafy(current.x);
        current.y = deaccurafy(current.y);
        current.z = deaccurafy(current.z);
        current.timeStamp = now;

        KLocation current = this.current.clone(), lastLoc = this.lastLoc;
        for(int inc = 3 ; inc > 0 ; --inc) {
            double x = current.x + (lastLoc.x - current.x) / inc;
            double y = current.y + (lastLoc.y - current.y) / inc;
            double z = current.z + (lastLoc.z - current.z) / inc;

            KLocation loc = new KLocation(x, y, z);
            lastLoc = current;
            loc.timeStamp = current.timeStamp;
            current = loc;

            locations.add(loc);
            current.timeStamp+= 50;
        }
        return false;
    }

    private static double deaccurafy(double val) {
        return MathHelper.floor(val * 32.) / 32.;
    }

    /*@Packet
    public boolean onTrans(WrappedOutTransaction packet, long now) {
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
                            debug("reset");
                        }
                        //this.posX + (this.newPosX - this.posX) / (double) this.newPosRotationIncrements;
                        if (inc > 0) {

                           double x = current.x + (newLoc.x / 32. - current.x) / inc;
                            double y = current.y + (newLoc.y / 32. - current.y) / inc;
                            double z = current.z + (newLoc.z / 32. - current.z) / inc;

                            current = incremented = new KLocation(x, y, z);
                            current.timeStamp = now;

                            locs.add(current);

                            debug("%v x=%v.4 y=%v.4 z=%v.4", inc, incremented.x, incremented.y, incremented.z);

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
                            debug("%v x=%v.4 y=%v.4 z=%v.4", inc, incremented.x, incremented.y, incremented.z);

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
        return false;
    }*/

    @Packet
    public boolean onUse(WrappedInUseEntityPacket packet, long now) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            if (!(packet.getEntity() instanceof LivingEntity)) {
                target = null;
                return false;
            }

            if (target == null
                    || packet.getEntity().getEntityId() != target.getEntityId()) locations.clear();

            target = (LivingEntity) packet.getEntity();

            int ping = (data.lagInfo.transPing) * 50;

            for (int i = locations.size() - 1; i > 0; --i) {
                KLocation loc = locations.get(i);
                long delta = now - loc.timeStamp;
                if (delta - ping <= 600) {
                    debug("(%vms) x=%v y=%v z=%v", delta, loc.x, loc.y, loc.z);
                }
            }

            attacked = true;
        }
        return false;
    }
}
