package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInSteerVehiclePacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (H)", description = "Looks for invalid look packets", developer = true,
        checkType = CheckType.BADPACKETS, planVersion = KauriVersion.FREE)
public class BadPacketsH extends Check {

    private boolean exempt;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.creative) return;

        if(!packet.isPos() && packet.isLook()) {
            if(data.playerInfo.from.yaw == data.playerInfo.to.yaw
                    && data.playerInfo.from.pitch == data.playerInfo.to.pitch) {
                if(!exempt && data.playerInfo.lastTeleportTimer.isPassed(1)) {
                    vl++;
                    flag("yaw=[%s,%s] p=[%s,%s]", data.playerInfo.to.yaw, data.playerInfo.from.yaw,
                            data.playerInfo.to.pitch, data.playerInfo.from.pitch);
                }
                exempt = true;
            }
        }
    }

    @Packet
    public void onSteer(WrappedInSteerVehiclePacket packet) {
        exempt = true;
    }
}
