package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Velocity (D)", checkType = CheckType.VELOCITY)
public class VelocityD extends Check {

    private double vxz;
    private double ldx, ldz;
    private double x, z, lx, lz, deltaX, deltaZ;
    private int tick;
    private float friction;

    @Packet
    public boolean onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() != data.getPlayer().getEntityId()) return false;
        data.runKeepaliveAction(ka -> {
            vxz = Math.hypot(packet.getX(), packet.getZ());
            tick = 0;
        });
        return false;
    }

    @Packet
    public boolean onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) {
            return false;
        }

        lx = x;
        lz = z;
        x = packet.getX();
        z = packet.getZ();

        deltaX = x - lx;
        deltaZ = z - lz;

        double deltaXZ = Math.hypot(deltaX, deltaZ);

        if(deltaXZ == 0 || vxz == 0) {
            vxz = 0;
            return false;
        }

        if(data.playerInfo.liquidTimer.hasNotPassed(2)
                || data.blockInfo.blocksNear
                || data.playerInfo.webTimer.hasNotPassed(2)) {
            vxz = 0;
            tick = 0;
            return false;
        }

        double friction = this.friction, moveFactor = data.getPlayer().getWalkSpeed() / 2f;

        moveFactor+= moveFactor * 0.30000001192092896D;

        if(data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED)) {
            moveFactor += (data.potionProcessor.getEffectByType(PotionEffectType.SPEED).getAmplifier() + 1)
                    * 0.20000000298023224D * moveFactor;
        }

        if(data.playerInfo.lClientGround) {
            friction*= 0.91;
            moveFactor/= Math.pow(friction, 3);
        } else {
            friction = 0.91;
            moveFactor = 0.026;
        }

        vxz -= moveFactor / (0.98 * 0.98);

        double ratio = 1 - ((vxz - deltaXZ) - moveFactor);

        debug("ratio=%v.4 deltaXZ=%v.4 move=%v, vxz=%v.4 friction=%v tick=%v",
                ratio, deltaXZ, moveFactor, vxz, friction, tick);

        if(ratio < 0.95) {
            vl++;
            if(vl > 5) {
                flag("pct=%v.1", ratio * 100);
            }
        } else if(vl > 0) vl-= 2;

        vxz*= friction;
        this.friction = data.blockInfo.currentFriction;

        if(++tick > 3 || Math.abs(vxz) < 0.005) {
            vxz = 0;
            tick = 0;
        }
        return false;
    }
}
