package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
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
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;

@CheckInfo(name = "Phase", description = "Ensures players cannot move through blocks.",
        checkType = CheckType.EXPLOIT, cancellable = true, enabled = false, executable = false,
        devStage = DevStage.ALPHA)
public class Phase extends Check {

    private KLocation fromWhereShitAintBad = null;
    private final Timer lastFlag = new TickTimer(5);
    private static final Set<Material> allowedMaterials = EnumSet.noneOf(Material.class);

    @Setting(name = "blacklistedMaterials")
    private static List<String> blacklistedMaterials = new ArrayList<>();

    static {
        Arrays.stream(Material.values())
            .filter(mat -> mat.name().contains("BANNER") || mat.name().contains("BREWING")
                    || mat.name().contains("CAULDRON") || mat.name().contains("PISTON"))
                .forEach(allowedMaterials::add);

        allowedMaterials.add(XMaterial.VINE.parseMaterial());
        allowedMaterials.add(XMaterial.CAKE.parseMaterial());
        if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)) {
            allowedMaterials.add(XMaterial.SCAFFOLDING.parseMaterial());
        }
    }

    public Phase() {
        blacklistedMaterials.stream().map(Material::getMaterial)
                .filter(m -> !allowedMaterials.contains(m))
                .forEach(allowedMaterials::add);
    }

    @Setting(name = "flagIntoChat")
    private boolean flagIntoChat = false;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(!packet.isPos() || now - data.creation < 800L || now - data.playerInfo.lastRespawn < 500L
                || data.playerInfo.moveTicks == 0
                || data.playerInfo.creative || data.playerInfo.canFly) {
            return;
        }

        TagsBuilder tags = new TagsBuilder();

        SimpleCollisionBox
                toUpdate = new SimpleCollisionBox(data.playerInfo.to.toVector(), 0.6,1.8)
                .expand(-0.0825),
                playerBox = new SimpleCollisionBox(data.getPlayer().getLocation(), 0.6, 1.8)
                        .expand(-0.0825);

        SimpleCollisionBox concatted = Helper.wrap(playerBox, toUpdate);

        List<Block> blocks = dev.brighten.anticheat.utils.Helper.getBlocks(data.blockInfo.handler, concatted);

        phaseIntoBlock: {
            List<Block> current = Helper.blockCollisions(blocks, playerBox),
                    newb = Helper.blockCollisions(blocks, toUpdate);

            for (Block block : newb) {
                if(!current.contains(block)) {
                    Material type = block.getType();
                    if(Materials.checkFlag(type, Materials.SOLID)
                            && !allowedMaterials.contains(type)
                            && !Materials.checkFlag(type, Materials.STAIRS)) {
                        tags.addTag("INTO_BLOCK");
                        tags.addTag("material=" + type.name());
                        vl++;
                        break;
                    } else debug(type.name());
                }
            }
        }
        
        phaseThru: {
            if(playerBox.isIntersected(toUpdate)) break phaseThru;
            
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

                } else {
                    List<SimpleCollisionBox> downcasted = new ArrayList<>();

                    box.downCast(downcasted);

                    boolean flagged = false;
                    for (SimpleCollisionBox sbox : downcasted) {
                        Tuple<Double, Double> result = new Tuple<>(0., 0.);
                        boolean intersected = RayCollision.intersect(ray, sbox);

                        if(intersected && result.one <= dist) {
                            flagged = true;
                            break;
                        }
                    }

                    if(flagged) {
                        vl++;
                        tags.addTag("THROUGH_BLOCK");
                        tags.addTag("material=" + type);
                        break;
                    }
                }
            }
        }

        if(tags.getSize() > 0) {
            flag("tags=%s", tags.build());
            final Location finalSetbackLocation = data.playerInfo.from.toLocation(data.getPlayer().getWorld());
            if(finalSetbackLocation != null) {
                RunUtils.task(() -> data.getPlayer().teleport(finalSetbackLocation));
            }
            lastFlag.reset();
        } else fromWhereShitAintBad = data.playerInfo.from;
    }
}
