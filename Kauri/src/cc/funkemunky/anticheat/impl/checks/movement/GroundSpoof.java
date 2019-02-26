package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class GroundSpoof extends Check {
    private int vl;
    private long lastTimeStamp;
    public GroundSpoof(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if (getData().getLastServerPos().hasNotPassed(2)
                || move.getTo().toVector().distance(move.getFrom().toVector()) < 0.005 || timeStamp < lastTimeStamp + 5)
            return;

        if (!getData().isGeneralCancel()) {
            if(move.isClientOnGround() != move.isServerOnGround()) {
                if((!move.isNearGround() && getData().getLastServerPos().hasPassed(6) && move.getAirTicks() > 2) || vl++ > 5) {
                    flag(getData().getMovementProcessor().isClientOnGround() + "!=" + getData().getMovementProcessor().isServerOnGround(), true, true);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }
            debug("VL: " + vl + "CLIENT: " + getData().getMovementProcessor().isClientOnGround() + " SERVER: " + getData().getMovementProcessor().isServerOnGround());
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
