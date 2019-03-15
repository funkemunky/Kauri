package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import lombok.var;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

@Packets(packets = {Packet.Client.USE_ENTITY})
public class KillauraG extends Check {
    public KillauraG(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);

        setDeveloper(true);
    }

    @Setting(name = "threshold.collision")
    private int threshold = 2;

    @Setting(name = "multipliers.blockBoxShrink")
    private double shrinkMult = 0.1;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if (!use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) return;

        if (use.getEntity() instanceof LivingEntity) {

            val origin = getData().getPlayer().getLocation().add(0, 1.53, 0);
            val entity = (LivingEntity) use.getEntity();

            val pitchOffset = MathUtils.getOffsetFromLocation(getData().getPlayer().getLocation(), entity.getLocation())[1];

            val distance = origin.distance(((LivingEntity) use.getEntity()).getEyeLocation()) / 1.6 - 0.2;

            if (pitchOffset > 35 || distance < 1 || !getData().getMovementProcessor().isServerOnGround()) return;


            RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

            //TODO Test with 0.5 and see if it has false positives. If so, put it back to 0.2.
            List<Vector> vectors = trace.traverse(distance, 0.2);

            //vectors.forEach(position -> origin.getWorld().playEffect(position.toLocation(origin.getWorld()), ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? Effect.SMOKE : Effect.valueOf("COLOURED_DUST"), 0));

            var amount = 0;

            val boxToCheck = new BoundingBox(getData().getPlayer().getEyeLocation().toVector(), use.getEntity().getLocation().toVector()).grow(1, 1, 1);

            val collidingBlocks = boxToCheck.getCollidingBlockBoxes(getData().getPlayer());

            val entityBox = MiscUtils.getEntityBoundingBox((LivingEntity) use.getEntity()).grow(0.35f, 0.35f, 0.35f);
            for (Vector vec : vectors) {
                if (entityBox.intersectsWithBox(vec)) {
                    break;
                }

                Block block = BlockUtils.getBlock(vec.toLocation(origin.getWorld()));
                if (!block.getType().isSolid() || BlockUtils.isClimbableBlock(block)) continue;

                float shrink = (float) shrinkMult;

                if (collidingBlocks.stream().anyMatch(box -> box.shrink(shrink, shrink, shrink).intersectsWithBox(vec))) {
                    amount++;
                }
            }

            if (amount > threshold) {
                flag(amount + ">-" + threshold, true, true);
            }

            debug("COLLIDED: " + amount + " AMOUNT: " + vectors.size() + " DISTANCE: " + MathUtils.round(distance, 4));
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
