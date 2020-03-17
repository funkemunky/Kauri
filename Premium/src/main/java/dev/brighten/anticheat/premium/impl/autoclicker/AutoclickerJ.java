package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, developer = true, maxVersion = ProtocolVersion.V1_8_9)
public class AutoclickerJ extends Check {

    private double buffer, lavg;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        val click = data.clickProcessor;
        if(click.getNosqrtKurtosis() < 6000 && click.getNosqrtStd() > 400
                && MathUtils.getDelta(click.getAverage(), lavg) > 0.5
                && MathUtils.getDelta(click.getNosqrtStd(), click.getNosqrtKurtosis()) > 1000) {
            if(buffer++ > 30) {
                vl++;
                flag("kurt=%v.3 avg=%v.3 std-%v.3 buffer=%v.1",
                        click.getNosqrtKurtosis(), click.getAverage(), click.getNosqrtStd(), buffer);
            }
        } else buffer-= buffer > 0 ? 0.5 : 0;

        lavg = click.getAverage();
        debug("kurt=%v.3 avg=%v.3 std-%v.3 buffer=%v.1",
                click.getNosqrtKurtosis(), click.getAverage(), click.getNosqrtStd(), buffer);
    }
}
