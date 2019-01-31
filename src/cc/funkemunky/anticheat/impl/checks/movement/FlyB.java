package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class FlyB extends Check {
    public FlyB(String name, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, cancelType, maxVL, enabled, executable, cancellable);
    }

    private int vl;
    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getLastServerPos().hasNotPassed(2)) return packet;
       boolean isColliding = getData().getBoundingBox().grow(0.5f, 0.1f, 0.5f).getCollidingBlockBoxes(getData().getPlayer()).size() == 0 || getData().getMovementProcessor().getDistanceToGround() < 1.2;
        if(MathUtils.getDelta(getData().getMovementProcessor().getDeltaY(), getData().getMovementProcessor().getServerYVelocity()) > 0.1
                && getData().getMovementProcessor().getDeltaY() > -2.5
                && !getData().getPlayer().getAllowFlight()
                && !getData().isAbleToFly()
                && !getData().getMovementProcessor().isRiptiding()
                && getData().getMovementProcessor().getLastRiptide().hasPassed(10)
                && !PlayerUtils.isGliding(getData().getPlayer())
                && getData().getMovementProcessor().getDeltaY() > getData().getMovementProcessor().getServerYVelocity() + 0.001
                && getData().getPlayer().getVehicle() == null
                && getData().getLastBlockPlace().hasPassed(10)
                && (getData().getMovementProcessor().getDistanceToGround() > 2.5 || getData().getMovementProcessor().getAirTicks() > 1)
                && getData().getMovementProcessor().getDistanceToGround() > 1.0
                && getData().getMovementProcessor().getLiquidTicks() == 0
                && getData().getVelocityProcessor().getLastVelocity().hasPassed(10)
                && getData().getMovementProcessor().getClimbTicks() == 0
                && !getData().getMovementProcessor().isServerOnGround()
                && MathUtils.getDelta(Math.abs(getData().getMovementProcessor().getClientYAcceleration()), Math.abs(getData().getMovementProcessor().getServerYAcceleration())) > 0.02) {
            if(!isColliding || vl++ > 3) {
                flag(getData().getMovementProcessor().getDeltaY() + ">-" + getData().getMovementProcessor().getServerYVelocity(), true, true);
            }
        } else {
            vl -= vl > 0 ? 1 : 0;
        }

        debug("MOTIONY: " + MathUtils.round(getData().getMovementProcessor().getDeltaY(), 4) + " SERVERY: " + MathUtils.round(getData().getMovementProcessor().getServerYVelocity(), 4));
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
