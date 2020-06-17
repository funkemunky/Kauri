package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumDirection;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.util.Vector;

@CheckInfo(name = "Hand (G)", description = "Checks if block is placed without proper positioning.",
        checkType = CheckType.HAND, developer = true)
public class HandG extends Check {

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        if(packet.getFace().equals(WrappedEnumDirection.UP)
                || packet.getFace().equals(WrappedEnumDirection.DOWN)) return;

        if(packet.getPosition().getY() >= data.playerInfo.to.y) return;

        val optional = data.blockInfo.belowCollisions.stream().filter(box ->
                box.isCollided(new SimpleCollisionBox(
                        new Vector(
                                packet.getPosition().getX(),
                                packet.getPosition().getY(),
                                packet.getPosition().getZ()), 1E-6, 1E-5))).findFirst();

        //This allows us to check if the block placed is colliding below the player.
        if (optional.isPresent()) {
            SimpleCollisionBox belowBox = optional.get();

            //If true, player is not on the edge of the block.
            if(data.box.copy().expand(-.3, 0.01, -.3).isCollided(belowBox)) {
                debug("not on edge");
            } else debug("on edge");
        } else debug("not below");

        debug("x=%v y=%v z=%v", packet.getPosition().getX(),
                packet.getPosition().getY(), packet.getPosition().getZ());
    }
}
