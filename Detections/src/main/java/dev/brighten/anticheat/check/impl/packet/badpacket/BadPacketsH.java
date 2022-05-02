package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInSteerVehiclePacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "BadPackets (H)", description = "Looks for invalid look packets", devStage = DevStage.BETA,
        checkType = CheckType.BADPACKETS, executable = true, punishVL = 9)
public class BadPacketsH extends Check {

    private boolean exempt;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now ) {
        if(data.playerInfo.creative) return;

        if(!packet.isPos() && packet.isLook()) {
            if(data.playerInfo.from.yaw == data.playerInfo.to.yaw
                    && data.playerInfo.from.pitch == data.playerInfo.to.pitch) {
                if(!exempt && data.playerInfo.lastTeleportTimer.isPassed(1)
                        && data.playerInfo.vehicleTimer.isPassed(1)
                        && now - data.creation > 5000L
                        && data.playerInfo.lastRespawnTimer.isPassed(10)) {
                    vl++;
                    flag("yaw=[%s,%s] p=[%s,%s]", data.playerInfo.to.yaw, data.playerInfo.from.yaw,
                            data.playerInfo.to.pitch, data.playerInfo.from.pitch);
                }
                exempt = false;
            }
        } else exempt = true;
    }

    @Packet
    public void onSteer(WrappedInSteerVehiclePacket packet) {
        exempt = true;
    }
}
