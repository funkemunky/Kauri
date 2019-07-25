package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@Packets(packets = {
        Packet.Client.LOOK,
        Packet.Client.POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type D)", description = "Looks for a common angle mistake in clients. By Itz_Lucky.", type = CheckType.AIM, executable = true, maxVL = 10)
public class AimD extends Check {

    private Verbose verbose = new Verbose();
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (getData().isServerPos() || move.isServerPos() || getData().getLastLogin().hasNotPassed(20)) return;

        Vector vector = new Vector(move.getTo().getX() - move.getFrom().getX(), 0, move.getTo().getZ() - move.getFrom().getZ());
        double angleMove = vector.distanceSquared((new Vector(move.getTo().getYaw() - move.getFrom().getYaw(), 0, move.getTo().getYaw() - move.getFrom().getYaw())));

        if (angleMove > 100000 && move.getDeltaXZ() > 0.2f && move.getDeltaXZ() < 1) {
            if(verbose.flag(5, 1000L)) {
                flag("angle: " + angleMove, true, true, AlertTier.CERTAIN);
            } else if(verbose.getVerbose() > 2) {
                flag("angle: " + angleMove, true, true, AlertTier.HIGH);
            }
        }

        debug("angle: " + angleMove);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}