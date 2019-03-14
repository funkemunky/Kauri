package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@BukkitEvents(events = {PlayerInteractEvent.class})
public class Interact extends Check {
    public Interact(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerInteractEvent e = (PlayerInteractEvent) event;

        if (e.getAction().toString().contains("BLOCK")) {
            Block block = e.getClickedBlock();
            val origin = e.getPlayer().getLocation().clone().add(0, 1.53, 0);

            val direction = MathUtils.getRotations(origin, block.getLocation().add(0.2f, 0.2f, 0.2f));

            origin.setYaw(direction[0]);
            origin.setPitch(direction[1]);

            val distance = origin.distance(block.getLocation());

            if (distance < 1 || MathUtils.getVerticalDistance(e.getPlayer().getLocation(), block.getLocation()) < 0.35)
                return;

            if (distance > 12) {
                flag("Out of bounds", true, true);
                return;
            }

            RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

            List<Vector> vectors = trace.traverse(origin.distance(block.getLocation()) - 0.5f, 0.2);

            List<BoundingBox> collidedBlocks = new ArrayList<>();

            vectors.forEach(vec -> {
                List<BoundingBox> collided = getBox(vec).getCollidingBlockBoxes(getData().getPlayer());

                if (collided.size() > 0) {
                    collidedBlocks.addAll(collided);
                }
            });

            long count = vectors.stream().filter(vec -> collidedBlocks.stream().anyMatch(box -> box.shrink(0.2f, 0.2f, 0.2f).intersectsWithBox(vec))).count();

            debug("Count: " + count);
        }
    }

    private BoundingBox getBox(Vector vector) {
        return new BoundingBox(vector, vector).grow(0.1f, 0.1f, 0.1f);
    }
}
