package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.TickTimer;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
//@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type I)", maxVL = 25, executable = true, type = CheckType.AIM)
public class AimI extends Check {

    private int ticks = 0;
    private double vl;
    private TickTimer lastFlag = new TickTimer(5);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val div = (move.getYawGCD() / (double) move.getOffset());

        if(move.getYawDelta() < 0.1) return;

        if ((move.getYawDelta() / div % 1 == 0 || move.getLastYawDelta() / div % 1 == 0)) {
            debug(Color.Green + "Ticks: " + ticks);

            ticks = 0;
        } else ticks++;

        debug("ticks=" + ticks + " [" + (move.getYawDelta() / div) + ", " + (move.getLastYawDelta() / div) + "]");

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
