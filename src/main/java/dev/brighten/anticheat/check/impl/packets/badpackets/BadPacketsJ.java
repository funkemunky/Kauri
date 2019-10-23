package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (J)", description = "Checks for omni sprint.", checkType = CheckType.BADPACKETS)
public class BadPacketsJ extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            if(!data.predictionService.key.contains("W")
                    && !data.blockInfo.collidesHorizontally
                    && data.playerInfo.deltaXZ > 0.1
                    && data.playerInfo.sprinting
                    && data.getPlayer().isSprinting()
                    && !data.predictionService.key.equals("Nothing")) {
                if(vl++ > 5) {
                    flag("key=" + data.predictionService.key + "; sprinting");
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("sprint=" + data.playerInfo.sprinting
                    + " key=" + data.predictionService.key + " vl=" + vl);
        }
    }

}
