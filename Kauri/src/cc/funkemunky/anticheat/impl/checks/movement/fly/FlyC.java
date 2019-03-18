package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class FlyC extends Check {

    public FlyC(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.cancelForFlight(getData(), 12, true)) return;

        val move = getData().getMovementProcessor();

        if (move.isNearGround()) return;

        val collides = getData().getBoundingBox().grow(1.5f, 1.5f, 1.5f).getCollidingBlocks(getData().getPlayer()).stream().anyMatch(BlockUtils::isSolid);

        if (!MathUtils.approxEquals(0.01, move.getLastClientYAcceleration(), move.getClientYAcceleration())) {
            if ((!collides && move.getAirTicks() > 2) || vl++ > 4) {
                flag(move.getClientYAcceleration() + ", " + move.getLastClientYAcceleration(), true, true);
            }
        } else vl -= vl > 0 ? 0.75 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
