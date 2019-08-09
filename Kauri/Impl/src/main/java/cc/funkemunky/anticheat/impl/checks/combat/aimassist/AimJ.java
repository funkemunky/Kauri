package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimJ extends Check {

    private double vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getYawDelta() == 0) return;

        float offset = (move.getYawGCD() / (float) move.getOffset());
        if(move.getYawDelta() != move.getLastYawDelta()
                && move.getYawDelta() < 12
                && move.getLastYawDelta() < 12
                && offset < 0.05
                && (MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta()) / offset % 1 != 0)
                && move.getYawGCD() == move.getLastYawGCD()) {
            if(vl++ > 3) {
                flag("g=" + move.getYawGCD() + " y1=" + move.getYawDelta() + " y2=" + move.getLastYawDelta(), true, true, AlertTier.HIGH);
            }
            debug(Color.Green + "Flag: " + "yaw=" + move.getYawDelta() + " lastYaw=" + move.getLastYawDelta() + " gcd=" + (move.getYawGCD() / (float) move.getOffset()) + " vl=" + vl);
        } else vl -= vl > 0 ? 0.02 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
