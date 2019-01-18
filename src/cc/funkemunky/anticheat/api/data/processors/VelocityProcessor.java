package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import lombok.Getter;
import lombok.val;

@Getter
@Setting
public class VelocityProcessor {
    private float maxVertical, maxHorizontal, motionX, motionY, motionZ;
    private TickTimer lastVelocity = new TickTimer(40);

    public void update(WrappedOutVelocityPacket packet) {
        if(packet.getId() != packet.getPlayer().getEntityId()) return;

        maxVertical = motionY = (float) packet.getY();
        maxHorizontal = (float) Math.hypot(packet.getX(), packet.getZ());

        lastVelocity = new TickTimer(40);

        motionX = (float) packet.getX();
        motionZ = (float) packet.getZ();
    }

    public void update(WrappedInFlyingPacket packet) {
        motionX *= packet.isGround() ? 0.68f : 0.98f;
        motionZ *= packet.isGround() ? 0.68f : 0.98f;

        if(packet.isGround()) {
            motionY = 0;
        } else if(motionY > 0) {
            motionY -= 0.08f;
            motionY *= 0.98f;
        }

        if(motionX < 0.0005) {
            motionX = 0;
        }

        if(motionZ < 0.0005) {
            motionX = 0;
        }
    }
}
