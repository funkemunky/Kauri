package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type O)", description = "Checks for common denominators in the pitch.", maxVL = 50, cancelType = CancelType.MOTION, type = CheckType.AIM)
public class AimO extends Check {

    private int ticks = 0;
    private double vl;
    private TickTimer lastFlag = new TickTimer(5);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getLastAttack().hasPassed(3)) {
            if(getData().getLastAttack().hasPassed(10)) {
                ticks = 0;
            }
            return;
        }
        val move = getData().getMovementProcessor();
        val offset = 16777216L;
        val one = (long) (move.getYawDelta() * offset);
        val two = (long) (move.getLastYawDelta() * offset);
        val gcd = MiscUtils.gcd(one, two);
        val div = (gcd  / (double) offset);

        if(move.getYawDelta() / div % 1 == 0 && move.getLastYawDelta() / div % 1 == 0 || ticks > 30) {
           debug(Color.Green + "Ticks: " + ticks);

           if(ticks > 20) {
               if(vl++ > 7) {
                   flag("ticks: " + ticks + " vl: " + vl, true, true);
               }
               lastFlag.reset();
           } else if(ticks < 15 && lastFlag.hasPassed()) {
               vl-= vl > 0 ? 0.5 : 0;
               lastFlag.reset();
           } else if(lastFlag.hasPassed(2)){
               vl-= vl > 0 ? 0.2 : 0;
               lastFlag.reset();
           } else {
               vl-= vl > 0 ? 0.1 : 0;
           }

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
