package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import one.util.streamex.StreamEx;
import org.bukkit.event.Event;

import java.util.Comparator;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
@Init
@CheckInfo(name = "Killaura (Type E)", description = "Raytraces to check if there are blocks obstructing the path of attack.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, executable = false, developer = true)
public class KillauraE extends Check {

    //TODO Test for false positives.
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getTarget() != null && getData().getLastAttack().hasNotPassed(0) && !getData().isLagging()) {
            val move = getData().getMovementProcessor();
            val origin = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, 1.54, 0);
            val target = getData().getTarget();
            val distance = move.getTo().toVector().setY(0).distance(target.getLocation().toVector().setY(0));

            RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());
            val targetBox = MiscUtils.getEntityBoundingBox(target).grow(0.5f, 0.5f, 0.5f);

            val count = StreamEx.of(trace.traverse(distance / 1.5f, 0.25)).sorted(Comparator.comparing(vec -> vec.distance(origin.toVector()))).takeWhile(vec -> !targetBox.collides(vec) && vec.distance(origin.toVector()) < origin.distance(target.getEyeLocation())).filter(vec -> {
                val boxList = Atlas.getInstance().getBlockBoxManager().getBlockBox().getSpecificBox(vec.toLocation(target.getWorld()));

                return boxList.size() > 0 && boxList.stream().anyMatch(box -> box.intersectsWithBox(vec));
            }).count();

            if(count > 0) {
                flag("colliding=" + count, true, true, AlertTier.LIKELY);
            }

            debug("collidng=" + count);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}