package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Fly (Type B)", description = "Ensures the player does not move vertically faster than predicated.", type = CheckType.FLY, maxVL = 100)
public class FlyB extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val max = predicatedMaxHeight();
        val move = getData().getMovementProcessor();

        if(MiscUtils.cancelForFlight(getData(), -1, false)) return;

        if(move.getDeltaY() > max) {
            flag(move.getDeltaY() + ">-" + max, true, true, AlertTier.HIGH);
        }

        debug("y=" + move.getDeltaY() + " max=" + max);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private double predicatedMaxHeight() {
        val velocity = getData().getVelocityProcessor();
        val move = getData().getMovementProcessor();

        float baseHeight = 0.42f;

        baseHeight+= PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.JUMP) * 0.11f;
        baseHeight+= move.isOnSlimeBefore() ? move.getSlimeHeight() : 0;

        return baseHeight;
    }
}
