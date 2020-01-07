package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.utils.KLocation;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.RayCollision;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerMoveEvent;

@CheckInfo(name = "Hand (D)", description = "Identifies common blocking patterns", checkType = CheckType.HAND,
        developer = true)
public class HandD extends Check {

    @Event
    public void onMove(PlayerMoveEvent event) {
        val to = event.getTo().clone();
        RayCollision collision = new RayCollision(to.toVector(), to.getDirection());
    }
}
