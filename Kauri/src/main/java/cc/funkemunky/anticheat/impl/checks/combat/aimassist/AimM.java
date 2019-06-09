package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type M)", description = "Checks if the relationship between yaw changes is a prime number.", type = CheckType.AIM, maxVL = 10, developer = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimM extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val offset = 16777216L;
        val yawGCD = MiscUtils.gcd((long) ((move.getYawDelta()) * offset), (long) ((getData().getMovementProcessor().getLastYawDelta()) * offset));

        if(MathUtils.isPrimeNumber(yawGCD)) {
            debug("primed");
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
