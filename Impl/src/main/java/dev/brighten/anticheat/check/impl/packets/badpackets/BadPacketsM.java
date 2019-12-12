package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (M)", checkType = CheckType.BADPACKETS, developer = true,
        description = "Checks to see if a player sends duplicate packets without lagging first.")
public class BadPacketsM extends Check {

    private int lagTicks;
    private long lastTimestamp = System.currentTimeMillis();
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {

        long delta = timeStamp - lastTimestamp;

        if(timeStamp - data.creation > 100L) {
            if(delta > 90L) {
                lagTicks = Math.round(delta / 50f) + 1;
            } else if(delta < 4) {
                lagTicks--;

                if(lagTicks < -1) {
                    flag("extra lag ticks: " + Math.abs(lagTicks));
                }
            }
        }
        lastTimestamp = timeStamp;
    }
}
