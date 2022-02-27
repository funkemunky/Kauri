package dev.brighten.anticheat.check.impl.free.packet;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "BadPackets (E)", description = "Looks for bad positional packets", checkType = CheckType.BADPACKETS,
        punishVL = 8, minVersion = ProtocolVersion.V1_9, devStage = DevStage.ALPHA)
public class BadPacketsE extends Check {

    private int transSinceFlying;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() && !packet.isLook() && data.playerInfo.lastTeleportTimer.isPassed(5)) {
            vl++;
            flag("%s", transSinceFlying);
            debug("sent flying without pos or look on 1.9+: %s", transSinceFlying);
        }
        transSinceFlying = 0;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        if(Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction()).isPresent()) {
            transSinceFlying++;
        }
    }
}
