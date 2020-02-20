package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.api.check.CheckType;
import org.bukkit.event.player.PlayerMoveEvent;

@CheckInfo(name = "BadPackets (N)", description = "Checks to see if netty channel was never injected",
        checkType = CheckType.BADPACKETS)
public class BadPacketsN extends Check {

    @Event
    public void onMove(PlayerMoveEvent event) {
        if(!TinyProtocolHandler.getInstance().hasInjected(event.getPlayer())) {
            if(vl++ > 10) {
                flag("has not injected");
            }
        }
    }
}
