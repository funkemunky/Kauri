package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.math.RollingAverage;
import lombok.val;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.USE_ENTITY,
        Packet.Client.LOOK,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Killaura (Type F)", description = "A simple angle consistency check.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, cancellable = false, executable = false)
public class KillauraF extends Check {

    private RollingAverage average = new RollingAverage(20);
    private LivingEntity target;
    private TickTimer timer = new TickTimer(4);
    private float vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if (!(use.getEntity() instanceof LivingEntity)) return;

            target = (LivingEntity) use.getEntity();
            timer.reset();
        } else if (target != null && timer.hasNotPassed()) {
            val player = getData().getPlayer();
            val offsetArray = MathUtils.getOffsetFromEntity(getData().getPlayer(), target);

            val yawDelta = MathUtils.yawTo180F(MathUtils.getDelta(getData().getMovementProcessor().getFrom().getYaw(), getData().getMovementProcessor().getTo().getYaw()));

            double offset = offsetArray[0], average = this.average.getAverage();

            if (average < 5.0 && (player.isSprinting() || yawDelta > 2.0) && yawDelta > 0.3 && getData().getMovementProcessor().getDeltaXZ() > 0.15 && vl++ > 100) {
                flag(average + "<-4.0->" + vl, true, true, AlertTier.POSSIBLE);
            } else {
                vl -= vl > 0 ? 2f : 0;
            }

            debug(average + ", " + offset + ", " + vl);
            this.average.add(offset, System.currentTimeMillis());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
