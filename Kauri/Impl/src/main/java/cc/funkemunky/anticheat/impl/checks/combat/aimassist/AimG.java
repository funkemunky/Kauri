package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.TickTimer;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "AimA (Type G)", description = "A heuristic which looks for one thing all aimbots have in common.", maxVL = 50, cancelType = CancelType.MOTION, type = CheckType.AIM)
public class AimG extends Check {

    private int ticks = 0;
    private double vl;
    private TickTimer lastFlag = new TickTimer(5);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (getData().getLastAttack().hasPassed(10)) {
            if(getData().getLastAttack().hasPassed(20)) ticks = 0;
            return;
        }
        val move = getData().getMovementProcessor();
        val div = (move.getYawGCD() / (double) move.getOffset());

        if(move.getYawDelta() < 0.25) return;

        if (move.getYawDelta() / div % 1 == 0 && move.getLastYawDelta() / div % 1 == 0 || ticks > 30) {
            debug(Color.Green + "Ticks: " + ticks);

            if (ticks > 30) {
                if(vl++ > 11) {
                    flag("ticks: " + ticks + " vl: " + vl, true, true, AlertTier.HIGH);
                } else if (vl > 7) {
                    flag("ticks: " + ticks + " vl: " + vl, true, true, AlertTier.LIKELY);
                } else if(vl > 5) {
                    flag("ticks: " + ticks + " vl: " + vl, true, false, AlertTier.POSSIBLE);
                }
                lastFlag.reset();
            } else vl-= vl > 0 ? 1 : 0;

            debug(Color.Green + "VL: " + vl);

            ticks = 0;
        } else ticks++;

        //debug(0.1 - (move.getYawDelta() / div) % 0.1 + "");

        //debug("GCD: " + gcd + " div: " + div + " 1: " + (move.getYawDelta() / div));
        //debug("1: " + (move.getYawDelta() / div) + " 2: " + (move.getLastYawDelta() / div));
        //debug("1: " + (move.getYawDelta() / move.getLastYawDelta() / div));
        //debug("LCM: " + lcm);
        //debug("1: " + (one / (double) lcm) + " 2: " + (two / (double) lcm));
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
