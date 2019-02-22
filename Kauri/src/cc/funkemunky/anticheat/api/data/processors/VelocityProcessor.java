package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import lombok.Getter;
import lombok.val;
import lombok.var;

@Getter
public class VelocityProcessor {
    private float maxVertical, maxHorizontal, motionX, motionY, motionZ, lastMotionX, lastMotionY, lastMotionZ;
    private TickTimer lastVelocity = new TickTimer(40);

    public void update(WrappedOutVelocityPacket packet) {
        if(packet.getId() != packet.getPlayer().getEntityId()) return;

        maxVertical = motionY = (float) packet.getY();
        maxHorizontal = (float) Math.hypot(packet.getX(), packet.getZ());

        lastVelocity.reset();

        motionX = (float) packet.getX();
        motionZ = (float) packet.getZ();
    }

    public void update(WrappedInFlyingPacket packet) {
        var motionX = this.motionX;
        var motionZ = this.motionZ;
        var motionY = this.motionY;

        var multiplier = 0.91f;

        if(packet.isGround()) multiplier*= 0.68f;

        motionX *= multiplier;
        motionZ *= multiplier;

        if(packet.isGround()) {
            motionY = 0;
        } else if(motionY > 0) {
            motionY -= 0.08f;
            motionY *= 0.98f;
        }

        if(motionY < 0.0005) {
            motionY = 0;
        }

        if(motionX < 0.0005) {
            motionX = 0;
        }

        if(motionZ < 0.0005) {
            motionZ = 0;
        }
        lastMotionX = this.motionX;
        lastMotionY = this.motionY;
        lastMotionZ = this.motionZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }
}
