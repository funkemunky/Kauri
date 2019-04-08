package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
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

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val offset = 16777216L;
        val one = (long) (move.getYawDelta() * offset);
        val two = (long) (move.getLastYawDelta() * offset);
        val gcd = MathUtils.gcd(one, two);
        val div = (gcd / (double) offset);

        if((move.getYawDelta() / div) % 1 == 0) {
           // debug(Color.Green + "Flag");
        }

        debug("GCD: " + gcd + " div: " + div + " 1: " + (move.getYawDelta() / div));
        //debug("1: " + Color.Green + (move.getYawDelta() / div) + Color.Gray + " 2: " + Color.Green + (move.getLastYawDelta() / div));
        //debug("1: " + (move.getYawDelta() / move.getLastYawDelta() / div));
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
