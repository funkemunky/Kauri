package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@BukkitEvents(events = {PlayerMoveEvent.class})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type H)", description = "Looks for a common angle mistake in clients. By Itz_Lucky.", type = CheckType.AIM, cancelType = CancelType.MOTION, maxVL = 10)
public class AimH extends Check {

    public AimH() {

    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        if(e.getTo().getYaw() == e.getFrom().getYaw() || e.getTo().getPitch() == e.getFrom().getPitch()) return;

        if (getData().getLastServerPos().hasNotPassed(0) && getData().getLastLogin().hasNotPassed(20)) return;

        double deltaX = e.getTo().getX() - e.getFrom().getX(), deltaZ = e.getTo().getZ() - e.getFrom().getZ(), yawDelta = e.getTo().getYaw() - e.getFrom().getYaw();

        Vector vector = new Vector(deltaX, 0, deltaZ);
        double angleMove = vector.distanceSquared((new Vector(yawDelta, 0, yawDelta)));

        if (angleMove > 100000 && deltaX > 0.2f && deltaZ < 1) {
            flag("angle: " + angleMove, true, true);
        }

        debug("angle: " + angleMove);
    }
}
