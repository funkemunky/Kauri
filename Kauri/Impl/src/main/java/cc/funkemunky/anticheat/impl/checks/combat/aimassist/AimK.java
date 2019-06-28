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
@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK})
@CheckInfo(name = "Aim (Type K)", description = "Looks for prime number in look movements", type = CheckType.AIM, developer = true)
public class AimK extends Check {

    float lastDelta, lastPitchDelta;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float delta = MiscUtils.convertToMouseDelta(move.getYawDelta()), pitchDelta = MiscUtils.convertToMouseDelta(move.getPitchDelta());
        float pitch = move.getPitchDelta();
        long gcd = MiscUtils.gcd((long) (delta * 16777216L), (long) (lastDelta * 16777216L)), pitchGCD = MiscUtils.gcd((long) (pitchDelta * 16777216L), (long) (lastPitchDelta * 16777216L));

        debug("ygcd=" + gcd + " pgcd" + pitchGCD + ", " + MathUtils.isPrimeNumber(gcd) + ", " + MathUtils.isPrimeNumber(pitchGCD));

        lastDelta = delta;
        lastPitchDelta = pitchDelta;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
