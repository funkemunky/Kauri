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
@CheckInfo(name = "Fly (Type E)", description = "Checks if a client moves vertically faster than what is possible.", type = CheckType.FLY, cancelType = CancelType.MOTION, executable = false, developer = true)
public class FlyE extends Check {
    public FlyE(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);

        setDeveloper(true);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        if (MiscUtils.cancelForFlight(getData(), 15, false)) return;

        val deltaY = (float) (e.getTo().getY() - e.getFrom().getY());
        val player = e.getPlayer();
        val totalMaxY = 0.6 + PlayerUtils.getPotionEffectLevel(player, PotionEffectType.JUMP) * 0.12f;

        if (deltaY > totalMaxY) {
            flag(deltaY + ">-" + totalMaxY, true, true);
        }
    }
}
