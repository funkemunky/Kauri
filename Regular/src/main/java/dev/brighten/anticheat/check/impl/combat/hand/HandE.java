package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.types.BaseBlockPosition;
import cc.funkemunky.api.utils.KLocation;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (E)", description = "Checks for common scaffolding patterns.", checkType = CheckType.HAND,
        punishVL = 15,
        developer = true)
public class HandE extends Check {

    private BaseBlockPosition lastBlock;
    private float lastYaw;
    private float lastPitch;
    private float lastX;
    private float lastY;
    private float lastZ;
    private double buffer;
    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet) {
        if (data.pastLocation.previousLocations.isEmpty()) return;
        BaseBlockPosition blockPosition = packet.getPosition();
        float x = packet.getVecX();
        float y = packet.getVecY();
        float z = packet.getVecZ();
        if (this.lastBlock != null && (blockPosition.getX() != this.lastBlock.getX() || blockPosition.getY() != this.lastBlock.getY() || blockPosition.getZ() != this.lastBlock.getZ())) {
            KLocation location = data.pastLocation.getLast();
            final float deltaAngle = Math.abs(this.lastYaw - location.yaw) + Math.abs(this.lastPitch - location.pitch);
            if (this.lastX == x && this.lastY == y && this.lastZ == z) {
                if (deltaAngle > 4.0f && ++buffer >= 4.0) {
                    vl++;
                    flag("x=%.1ff,y=%.1ff,z=%.1ff,d=%.1ff,d=%.1ff", x, y, z, deltaAngle, buffer);
                }
            } else buffer-= buffer > 0 ? 0.5 : 0;
            debug("x=%.1ff,y=%.1ff,z=%.1ff,d=%.1ff,d=%.1ff", x, y, z, deltaAngle, buffer);
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
            this.lastYaw = location.yaw;
            this.lastPitch = location.pitch;
        }
        this.lastBlock = blockPosition;
    }
}
