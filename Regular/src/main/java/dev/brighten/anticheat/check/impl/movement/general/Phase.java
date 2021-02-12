package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.check.CheckType;
import dev.brighten.db.utils.Pair;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Ensures players cannot move through blocks.",
        checkType = CheckType.EXPLOIT, cancellable = true, executable = false, developer = true)
public class Phase extends Check {

    private KLocation fromWhereShitAintBad = null;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(now - data.creation < 1200L || now - data.playerInfo.lastRespawn < 150L || data.playerInfo.serverPos)
            return;

        Location setbackLocation = null;
        TagsBuilder tags = new TagsBuilder();

        phaseIntoBlock: {
            SimpleCollisionBox currentBox = data.box,
                    fromBox = new SimpleCollisionBox(data.playerInfo.from.toVector(), 0.6, 1.8);

            boolean didCollide =
                    Helper.getBlocksNearby2(packet.getPlayer().getWorld(), fromBox, Materials.SOLID)
                            .stream().anyMatch(block -> fromBox.isIntersected(BlockData.getData(block.getType())
                            .getBox(block, data.playerVersion)));

            if(didCollide) {
                debug("collided");
                break phaseIntoBlock;
            }

            String[] boxes = Helper.getBlocksNearby2(packet.getPlayer().getWorld(), currentBox, Materials.SOLID)
                    .stream().filter(block -> fromBox.isIntersected(BlockData.getData(block.getType())
                            .getBox(block, data.playerVersion))).map(block -> block.getType().name())
                    .toArray(String[]::new);

            if(boxes.length > 0) {
                tags.addTag("INTO_BLOCK");
                vl++;
                for (String box : boxes)
                    tags.addTag(box);
                setbackLocation = data.playerInfo.from.toLocation(data.getPlayer().getWorld());
            }
        }

        if(tags.getSize() > 0) {
            flag("tags=%s", tags.build());

            if(setbackLocation != null) {
                Location finalSetbackLocation = setbackLocation;
                RunUtils.task(() -> data.getPlayer().teleport(finalSetbackLocation));
            }
        }
    }
}
