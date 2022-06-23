package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;

@CheckInfo(name = "BadPackets (D)",
        description = "Checks for clients spoofing flight permissions.",
        checkType = CheckType.BADPACKETS, punishVL = 10, executable = true)
@Cancellable
public class BadPacketsD extends Check {

    boolean serverAllowed, clientAllowed;

    @Packet
    public void server(WrapperPlayServerPlayerAbilities packet) {
        if(packet.isFlightAllowed()) {
            serverAllowed = true;
        } else if(!clientAllowed) {
            serverAllowed = false;
        }
    }

    @Packet
    public void client(WrapperPlayClientPlayerAbilities packet) {
        packet.isFlightAllowed().ifPresent(canFly -> {
            if(canFly) {
                clientAllowed = true;
            } else if(!serverAllowed) {
                clientAllowed = false;
            }
        });
    }

    @Packet
    public void flying(WrapperPlayClientPlayerFlying packet, long timeStamp) {
        if(timeStamp - data.creation < 1000L) {
            serverAllowed = data.getPlayer().getAllowFlight();
            clientAllowed = data.getPlayer().getAllowFlight();
            RunUtils.task(() -> {
                data.getPlayer().setAllowFlight(true);
                data.getPlayer().setAllowFlight(false);
                data.getPlayer().setGameMode(GameMode.SURVIVAL);
            }, Kauri.INSTANCE);
        } else {
            if(!serverAllowed && clientAllowed) {
                if(vl++ > 1) {
                    flag("server=" + serverAllowed + " client=" + clientAllowed);
                    RunUtils.task(() -> {
                        data.getPlayer().setFlying(false);
                        data.getPlayer().setAllowFlight(false);
                    }, Kauri.INSTANCE);
                }
            } else vl = 0;
        }
    }
}
