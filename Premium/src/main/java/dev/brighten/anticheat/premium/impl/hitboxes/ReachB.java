package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityTeleportPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.util.Vector;

import java.util.UUID;

@CheckInfo(name = "Reach (B)", planVersion = KauriVersion.ARA, developer = true, checkType = CheckType.HITBOX)
public class ReachB extends Check {

    private EntityLocation eloc = new EntityLocation(UUID.randomUUID());
    private Timer lastFlying = new AtlasTimer();
    private int streak;
    private float buffer;
    private boolean sentTeleport, attacked;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(data.target == null || packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
            return;

        //Updating new entity loc
        if(data.target.getUniqueId() != eloc.uuid) {
            eloc = new EntityLocation(data.target.getUniqueId());
            sentTeleport = false;
        }

        attacked = true;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(attacked) {
            attacked = false;
            KLocation eyeLoc = data.playerInfo.to.clone();

            eyeLoc.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;

            if(eloc.x == 0 && eloc.y == 0 & eloc.z == 0) return;

            SimpleCollisionBox targetBox = (SimpleCollisionBox) EntityData
                    .getEntityBox(new Vector(eloc.x, eloc.y, eloc.z), data.target);

            if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                targetBox = targetBox.expand(0.1, 0.1, 0.1);
            }

            AxisAlignedBB vanillaBox = new AxisAlignedBB(targetBox);

            val intersect = vanillaBox.rayTrace(eyeLoc.toVector(), MathUtils.getDirection(eyeLoc), 10);

            if(intersect != null) {
                double distance = new Vector(intersect.x, intersect.y, intersect.z).distance(eyeLoc.toVector());

                if(distance > 3 && streak > 7 && sentTeleport && lastFlying.isNotPassed(1)) {
                    if(++buffer > 4) {
                        vl++;
                        flag("d=%.4f", distance);
                        buffer = 2;
                    }
                } else if(buffer > 0) buffer-= 0.1f;
                debug("dist=%.2f", distance);
            } else debug("didnt hit box: x=%.1f y=%.1f z=%.1f", eloc.x, eloc.y, eloc.z);
        }


        eloc.interpolateLocation();
        if(lastFlying.isNotPassed(1)) streak++;
        else {
            streak = 1;
            sentTeleport = false;
        }

        lastFlying.reset();
    }

    @Packet
    public void onEntity(WrappedOutRelativePosition packet, int now) {
        if(data.target != null && data.target.getEntityId() == packet.getId()) {
            data.runInstantAction(() -> {
                //We don't need to do version checking here. Atlas handles this for us.
                if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
                    eloc.newX += (byte)packet.getX() / 32D;
                    eloc.newY += (byte)packet.getY() / 32D;
                    eloc.newZ += (byte)packet.getZ() / 32D;
                    eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                    eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
                } else if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_14)) {
                    eloc.newX += (long)packet.getX() / 4096D;
                    eloc.newY += (long)packet.getY() / 4096D;
                    eloc.newZ += (long)packet.getZ() / 4096D;
                    eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                    eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
                } else {
                    eloc.newX += (short)packet.getX() / 4096D;
                    eloc.newY += (short)packet.getY() / 4096D;
                    eloc.newZ += (short)packet.getZ() / 4096D;
                    eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                    eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
                }

                eloc.increment = 3;
            });
        }
    }

    @Packet
    public void onTeleport(WrappedOutEntityTeleportPacket packet, int now) {
        if(data.target != null && data.target.getEntityId() == packet.entityId) {

            data.runInstantAction(() -> {
                eloc.increment = 3;
                //We don't need to do version checking here. Atlas handles this for us.
                eloc.newX = eloc.x = packet.x;
                eloc.newY = eloc.y = packet.y;
                eloc.newZ = eloc.z = packet.z;
                eloc.newYaw = eloc.yaw = packet.yaw;
                eloc.newPitch = eloc.pitch = packet.pitch;

                //Clearing any old interpolated locations
                eloc.interpolatedLocations.clear();

                sentTeleport = true;

                debug("teleport: %s", MiscUtils.currentTick() - now);

                KLocation tploc = new KLocation(eloc.x, eloc.y, eloc.z, eloc.yaw, eloc.pitch,
                        Kauri.INSTANCE.keepaliveProcessor.tick);
                eloc.interpolatedLocations.add(tploc);
            });
        }
    }

}
