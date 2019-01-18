package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BlockUtils;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class NoFall extends Check {
    private boolean lastOnGround;
    private int verbose;
    private long lastTimeStamp;
    public NoFall(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getLastServerPos().hasNotPassed(1) || getData().getMovementProcessor().getTo().toVector().distance(getData().getMovementProcessor().getFrom().toVector()) < 0.005)
            return;
        WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

        if (!getData().isGeneralCancel()) {
            if (timeStamp - lastTimeStamp > 1) {
                if (!getData().getMovementProcessor().isServerOnGround()
                        && getData().getMovementProcessor().getAirTicks() > 3
                        && flying.isGround()
                        && !BlockUtils.getBlock(getData().getMovementProcessor().getTo().toLocation(getData().getPlayer().getWorld())).getType().isSolid()
                        && getData().getMovementProcessor().getDistanceToGround() > 3.0
                        && getData().getLastBlockPlace().hasPassed(5)) {
                    flag("t: air;" + lastOnGround + "!=" + flying.isGround(), true, false);
                }

                if (getData().getMovementProcessor().isServerOnGround() != flying.isGround()
                        && getData().getMovementProcessor().getAirTicks() > 4
                        && getData().getMovementProcessor().getGroundTicks() > 4
                        && !BlockUtils.getBlock(getData().getMovementProcessor().getTo().toLocation(getData().getPlayer().getWorld()).add(0, 0.1, 0)).getType().isSolid()) {
                    if (verbose++ > 6) {
                        flag("t: full;" + getData().getMovementProcessor().isServerOnGround() + "!=" + flying.isGround(), true, true);
                    }
                } else {
                    verbose = 0;
                }
            }
            lastTimeStamp = timeStamp;
        }

        lastOnGround = flying.isGround();
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
