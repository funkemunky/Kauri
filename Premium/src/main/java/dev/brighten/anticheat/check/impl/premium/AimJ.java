package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@CheckInfo(name = "Aim (J)",  description = "Detects gcd patches -Rhys", checkType = CheckType.AIM, vlToFlag = 5, developer = true)
public class AimJ extends Check {

    private static float sens = MovementProcessor.percentToSens(95);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            double o = MiscUtils.clampToVanilla(sens,
                    data.playerInfo.to.pitch);

            float pitch = data.playerInfo.to.pitch;

            int length = MiscUtils.length(o);

            if(Math.abs(pitch) != 90f && ((o > 0 && length > 0 && length < 8)
                    || (o == 0f && length > 0 && length < 6))) {
                vl++;
                flag(40, "o=%s t=%s", o, length);
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class Data {
        private final double threshold, pitch, sensitivity, offset, cache;
        private final long millis;
        private final int length;
    }
}
