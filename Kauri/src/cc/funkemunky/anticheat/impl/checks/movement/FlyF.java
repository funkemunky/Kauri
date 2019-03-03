package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

@BukkitEvents(events = {PlayerMoveEvent.class})
public class FlyF extends Check {
    public FlyF(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        val velocity = getData().getVelocityProcessor();

        if(velocity.getLastVelocity().hasPassed(3)) return;

        val deltaY = (float) (e.getTo().getY() - e.getFrom().getY());

        if(deltaY > velocity.getMaxVertical() + 0.01 && deltaY > 0.1 && !getData().getMovementProcessor().isNearGround()) {
            flag(deltaY + ">-" + velocity.getLastMotionY(), true, true);
        }
    }
}
