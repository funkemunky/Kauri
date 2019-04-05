package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

@BukkitEvents(events = {PlayerMoveEvent.class})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fly (Type D)", description = "Checks if a client moves vertically faster than what is possible.", type = CheckType.FLY, cancelType = CancelType.MOTION, executable = false, developer = true)
public class FlyD extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        if (MiscUtils.cancelForFlight(getData(), 6, false)) return;

        val deltaY = (float) (e.getTo().getY() - e.getFrom().getY());
        val player = e.getPlayer();
        val totalMaxY = 0.6 + getData().getVelocityProcessor().getMotionY() + (getData().getVelocityProcessor().getLastVelocity().hasNotPassed(30) ? getData().getVelocityProcessor().getMaxVertical() : 0) + PlayerUtils.getPotionEffectLevel(player, PotionEffectType.JUMP) * 0.12f;

        if (deltaY > totalMaxY) {
            flag(deltaY + ">-" + totalMaxY, true, true);
        }

        debug("Y: " + deltaY);
    }
}
