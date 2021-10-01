package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Ensures players cannot move through blocks.",
        checkType = CheckType.EXPLOIT, cancellable = true, executable = false, developer = true)
public class Phase extends Check {

    private KLocation fromWhereShitAintBad = null;
    private final Timer lastFlag = new TickTimer(5);
    private static final Set<Material> allowedMaterials = EnumSet.noneOf(Material.class);

    static {
        Arrays.stream(Material.values())
            .filter(mat -> mat.name().contains("BANNER") || mat.name().contains("BREWING"))
                .forEach(allowedMaterials::add);

        allowedMaterials.add(XMaterial.VINE.parseMaterial());
    }

    @Setting(name = "flagIntoChat")
    private boolean flagIntoChat = false;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(now - data.creation < 1200L || now - data.playerInfo.lastRespawn < 150L
                || data.playerInfo.doingTeleport
                || data.playerInfo.creative || data.playerInfo.canFly) {
            if(!data.playerInfo.checkMovement)
            debug("cant check movement: sp=%s lf=%s", data.playerInfo.doingTeleport, lastFlag.getPassed());
            return;
        }
        TagsBuilder tags = new TagsBuilder();

        SimpleCollisionBox toUpdate = data.box.copy().shrink(0.0625, 0.0625, 0.0625),
                playerBox = new SimpleCollisionBox(data.getPlayer().getLocation(), 0.6, 1.8)
                        .shrink(0.0625, 0.0625, 0.0625);

        SimpleCollisionBox concatted = Helper.wrap(toUpdate, playerBox);

        List<Block> blocks = dev.brighten.anticheat.utils.Helper.getBlocks(data.blockInfo.handler, concatted);

        phaseIntoBlock: {
            if(data.playerInfo.creative) break phaseIntoBlock;

            List<Block> current = Helper.blockCollisions(blocks, playerBox),
                    newb = Helper.blockCollisions(blocks, toUpdate.copy());

            for (Block block : newb) {
                if(!current.contains(block)) {
                    Material type = block.getType();
                    if(Materials.checkFlag(type, Materials.SOLID)
                            && !allowedMaterials.contains(type)
                            && !Materials.checkFlag(type, Materials.STAIRS)) {
                        tags.addTag("INTO_BLOCK");
                        vl++;
                        break;
                    }
                }
            }
        }
        
        phaseThru: {
            if(data.playerInfo.creative) break phaseThru;
            
            Vector to = data.playerInfo.to.toVector(), from = data.playerInfo.from.toVector();

            to.add(new Vector(0, data.playerInfo.sneaking ? 1.54f : 1.62f, 0));
            from.add(new Vector(0, data.playerInfo.lsneaking ? 1.54f : 1.62f, 0));

            double dist = to.distance(from);

            Vector direction = to.subtract(from);
            RayCollision ray = new RayCollision(from, direction);

            for (Block block : blocks) {
                Material type = block.getType();
                if(!Materials.checkFlag(type, Materials.SOLID)
                        || allowedMaterials.contains(type) || Materials.checkFlag(type, Materials.STAIRS))
                    continue;

                CollisionBox box = BlockData.getData(type).getBox(block, data.playerVersion);

                if(box instanceof SimpleCollisionBox) {
                    Tuple<Double, Double> result = new Tuple<>(0., 0.);
                    boolean intersected = RayCollision.intersect(ray, (SimpleCollisionBox) box);

                    if(intersected && result.one <= dist) {
                        vl++;
                        tags.addTag("THROUGH_BLOCK");
                        tags.addTag("material=" + type);
                        break;
                    }

                    debug("intersected=%s o=%.2f d=%.1f", intersected, result.one, dist);
                } else {
                    List<SimpleCollisionBox> downcasted = new ArrayList<>();

                    box.downCast(downcasted);

                    boolean flagged = false;
                    for (SimpleCollisionBox sbox : downcasted) {
                        Tuple<Double, Double> result = new Tuple<>(0., 0.);
                        boolean intersected = RayCollision.intersect(ray, sbox);

                        debug("intersected=%s o=%.2f d=%.1f", intersected, result.one, dist);

                        if(intersected && result.one <= dist) {
                            flagged = true;
                            break;
                        }
                    }

                    if(flagged) {
                        tags.addTag("THROUGH_BLOCK");
                        tags.addTag("material=" + type);
                        break;
                    }
                }
            }
        }

        clip: {
            if(data.playerInfo.canFly || data.playerInfo.creative) break clip;

            SimpleCollisionBox clipBox = data.box.copy().expand(data.playerInfo.deltaXZ, 0, data.playerInfo.deltaXZ);

            double threshold = data.potionProcessor.hasPotionEffect(PotionEffectType.JUMP) ? 0.62 : 0.5;

            if(data.blockInfo.pistonNear) threshold = 0.95;
            else if(data.playerInfo.blockAboveTimer.isNotPassed(20)) {
                //TODO Fix under block falses
                threshold = 0.8;
                if(data.playerInfo.iceTimer.isNotPassed(20)) threshold+= 0.4;
            }
            else if(data.playerInfo.jumped) threshold = 0.68;
            else if(data.playerInfo.iceTimer.isNotPassed(4)) threshold = 0.6;

            if(data.playerInfo.lastVelocity.isNotPassed(20))
                threshold = Math.max(threshold, Math.hypot(data.playerInfo.velocityX, data.playerInfo.velocityZ) + 0.3);

            Optional<PotionEffect> speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED);

            if(speed.isPresent()) {
                threshold*= 1.2 * (speed.get().getAmplifier() + 1);
            }

            if(data.playerInfo.deltaXZ > threshold) {
                //tags.addTag("CLIP");
                //vl++;
                //tags.addTag(String.format("%.3f>-%.3f", data.playerInfo.deltaXZ, threshold));
            }
        }

        if(tags.getSize() > 0) {
            flag("tags=%s", tags.build());

            final Location from = data.playerInfo.from.toLocation(data.getPlayer().getWorld());
            RunUtils.task(() -> data.getPlayer().teleport(from));
            lastFlag.reset();
        } else fromWhereShitAintBad = data.playerInfo.from;
    }
}
