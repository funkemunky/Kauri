package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Ensures players cannot move through blocks.",
        checkType = CheckType.EXPLOIT, cancellable = true, executable = false, developer = true)
public class Phase extends Check {

    private KLocation fromWhereShitAintBad = null;
    @Packet
    public boolean onFlying(WrappedInFlyingPacket packet, long now) {
        if(now - data.creation < 1200L || now - data.playerInfo.lastRespawn < 150L)
            return false;

        KLocation to = data.playerInfo.to, from = data.playerInfo.from;

        SimpleCollisionBox box = new SimpleCollisionBox(from.toVector(), to.toVector());

        box.expand(0.3, 0, 0.3);
        box.expandMax(0, 1.8, 0);
        box.expand(-0.0001, -0.1, -0.0001);

        val blockBoxes = data.blockInfo.blocks.stream()
                .filter(block -> Materials.checkFlag(block.getType(), Materials.SOLID))
                .map(block -> BlockData.getData(block.getType()).getBox(block, data.playerVersion))
                .collect(Collectors.toList());
        val boxes = blockBoxes.stream()
                .filter(box::isCollided).collect(Collectors.toList());

        val fromBox = new SimpleCollisionBox(data.playerInfo.from.toVector(), 0.6, 1.8)
                .expand(-0, -.05, -0);
        val fromCollided = boxes.stream().filter(bbox -> bbox.isCollided(fromBox))
                .collect(Collectors.toList());

        if(fromCollided.size() == 0) {
            fromWhereShitAintBad = data.playerInfo.from;
        }

        List<SimpleCollisionBox> debugMeAsshole = new ArrayList<>();
        boxes.forEach(bbox -> {
            bbox.downCast(debugMeAsshole);
            bbox.draw(WrappedEnumParticle.FLAME, Collections.singleton(packet.getPlayer()));
        });

        for (SimpleCollisionBox sbox : debugMeAsshole) {
            debug("minX=%v.3 minY=%v.3 minZ=%v.3 maxX=.%v3 maxY=%v.3 maxZ=%v.3",
                    sbox.xMin, sbox.yMin, sbox.zMin, sbox.xMax, sbox.yMax, sbox.zMax);
        }

        if((now - data.playerInfo.lastServerPos >= 10 || data.playerInfo.phaseFlagged)
                && fromCollided.size() == 0 && boxes.size() > 0) {
            data.playerInfo.phaseFlagged = true;
            RunUtils.task(() -> packet.getPlayer().teleport((fromWhereShitAintBad != null
                    ? fromWhereShitAintBad : from)
                    .toLocation(data.getPlayer().getWorld())));
            vl++;
            flag("boxes=%v", boxes.size());
            debug("fx=%v tx=%v", from.x, to.x);
            return false;
        } else if(boxes.size() > 0) {
            debug("from=%v boxes=%v", fromCollided.size(), boxes.size());
            data.playerInfo.phaseFlagged = false;
        } else data.playerInfo.phaseFlagged = false;
        /*for(int i = 0 ; i < distance ; i++) {
            double mult = i / 2.;
            double x = ray.originX + ray.directionX * mult;
            double y = ray.originY * ray.directionY * mult;
            double z = ray.originZ * ray.directionZ * mult;

            //debug
            WrappedPacketPlayOutWorldParticle particle =
                    new WrappedPacketPlayOutWorldParticle(WrappedEnumParticle.CRIT, true,
                            (float)x, (float)y, (float)z, 0, 0, 0, 0, 1);

            TinyProtocolHandler.sendPacket(packet.getPlayer(), particle);
            //end debug

            SimpleCollisionBox box = new SimpleCollisionBox(new Vector(x, y, z), 0.6, 1.8);

            //debug 2
            box.draw(WrappedEnumParticle.FLAME, Collections.singleton(packet.getPlayer()));
            //end debug 2
        }*/
        return false;
    }
}
