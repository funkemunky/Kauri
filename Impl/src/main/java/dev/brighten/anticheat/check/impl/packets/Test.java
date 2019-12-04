package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Test", description = "For development purposes.", checkType = CheckType.GENERAL, developer = true)
public class Test extends Check {

    private long lastTimestamp;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        debug("delta=" + delta + "ms ping=" + data.lagInfo.transPing + ", " + data.playerInfo.canFly + ", "
                + data.playerInfo.creative + " + " + data.playerInfo.worldLoaded + ", " + data.lagInfo.lastPacketDrop.getPassed() + ", " + data.lagInfo.lastPingDrop.getPassed() + ", " + (data.getPlayer().getVehicle() != null));
        lastTimestamp = timeStamp;
    }
}
