package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyD extends Check {
    public FlyD(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private double lastYChange;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getLastServerPos().hasNotPassed(1)) return;
        val move = getData().getMovementProcessor();
        val from = move.getFrom();
        val to = move.getTo();

        val yChange = to.getY() - from.getY();
        val predictedY = (lastYChange - 0.08D) * 0.9800000190734863D;
        this.lastYChange = yChange;

        if (MiscUtils.cancelForFlight(getData(), 10)) return;

        if (!move.isNearGround()) {
            val offset = Math.abs(yChange - predictedY);

            if (!MathUtils.approxEquals(0.00001, yChange, predictedY)) {
                if(vl++ > 2) {
                    this.flag("O -> " + offset, false, true);
                }
            } else {
                vl = Math.max(vl - 1, 0);
            }

            debug("VL: " + vl + "DIF: " + Math.abs(yChange - predictedY));
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
