package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (A)", description = "A fast click check.", checkType = CheckType.AUTOCLICKER,
        punishVL = 50)
public class AutoclickerA extends Check {

    private int ticks;
    private long lastClick;

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(timeStamp - lastClick > 1000L) {
            if(ticks > 30) {
                vl++;
                punish();
            } else if(ticks >= 19) {
                vl++;
                flag("cps=" + ticks + " ping=%p tps=%t");
            } else vl-= vl > 0 ? 0.2 : 0;
            ticks = 0;
            lastClick = timeStamp;
        } else if(data.playerInfo.lastBrokenBlock.hasPassed(5) && !data.playerInfo.breakingBlock) ticks++;
    }
}
