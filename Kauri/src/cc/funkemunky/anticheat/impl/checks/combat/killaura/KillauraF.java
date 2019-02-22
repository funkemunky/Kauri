package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
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
public class KillauraF extends Check {
    public KillauraF(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private RollingAverage average = new RollingAverage(20);
    private LivingEntity target;
    private TickTimer timer = new TickTimer(4);
    private float vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if(!(use.getEntity() instanceof LivingEntity)) return;

            target = (LivingEntity) use.getEntity();
            timer.reset();
        } else if(target != null && timer.hasNotPassed()) {
            val player = getData().getPlayer();
            val offsetArray = MathUtils.getOffsetFromEntity(getData().getPlayer(), target);

            val yawDelta = MathUtils.yawTo180F(MathUtils.getDelta(getData().getMovementProcessor().getFrom().getYaw(), getData().getMovementProcessor().getTo().getYaw()));

            double offset = offsetArray[0], average = this.average.getAverage();

            if (average < 5.0 && (player.isSprinting() || yawDelta > 2.0) && yawDelta > 0.15 && getData().getMovementProcessor().getDeltaXZ() > 0.15 && vl++ > 50) {
                flag(average + "<-4.0->" + vl, true, true);
            } else {
                vl-= vl > 0 ? 0.5f : 0;
            }

            debug(average + ", " + offset + ", " + vl);
            this.average.add(offset, System.currentTimeMillis());
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
