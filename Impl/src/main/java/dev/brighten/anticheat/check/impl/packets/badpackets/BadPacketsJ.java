package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@CheckInfo(name = "BadPackets (J)", description = "Checks for omni sprint.", checkType = CheckType.BADPACKETS,
        developer = true, punishVL = 100)
public class BadPacketsJ extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos() && !data.playerInfo.generalCancel) {
            if(data.playerInfo.sprinting
                    && data.playerInfo.clientGround
                    && !data.predictionService.key.equals("Nothing")
                    && !data.predictionService.key.contains("W")) {
                vl++;
                if(vl > 40) {
                    flag("key=%1 groundTicks=%2 lastVelocity=%3",
                            data.predictionService.key,
                            data.playerInfo.groundTicks,
                            data.playerInfo.lastVelocity.getPassed());
                }
            } else vl-= vl > 0 ? 0.5f : 0;
            debug("sprinting=" + data.playerInfo.sprinting
                    + " ground=" + data.playerInfo.clientGround
                    + " key=" + data.predictionService.key
                    + " velocity=" + data.playerInfo.lastVelocity.getPassed() + " vl=" + vl);
        } else vl-= vl > 0 ? 0.01f : 0;
    }

}
