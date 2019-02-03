package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
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
    private int vl;
    private long lastTimeStamp;
    public NoFall(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getLastServerPos().hasNotPassed(2) || getData().getMovementProcessor().getTo().toVector().distance(getData().getMovementProcessor().getFrom().toVector()) < 0.005)
            return packet;

        if (!getData().isGeneralCancel()) {
            if (timeStamp - lastTimeStamp > 1) {
                if(getData().getMovementProcessor().isClientOnGround() != getData().getMovementProcessor().isServerOnGround()) {
                    if((getData().getMovementProcessor().getDistanceToGround() > 2.0f && getData().getMovementProcessor().getAirTicks() > 4) || vl++ > 3) {
                        flag(getData().getMovementProcessor().isClientOnGround() + "!=" + getData().getMovementProcessor().isServerOnGround(), true, true);
                    }
                } else {
                    vl -= vl > 0 ? 1 : 0;
                }
                debug("VL: " + vl + "CLIENT: " + getData().getMovementProcessor().isClientOnGround() + " SERVER: " + getData().getMovementProcessor().isServerOnGround());
            }
            lastTimeStamp = timeStamp;
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
