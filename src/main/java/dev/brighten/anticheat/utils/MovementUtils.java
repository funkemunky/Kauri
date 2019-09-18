package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class MovementUtils {

    public static float getJumpHeight(Player player) {
        float baseHeight = 0.42f;

        if(player.hasPotionEffect(PotionEffectType.JUMP)) {
            baseHeight+= PlayerUtils.getPotionEffectLevel(player, PotionEffectType.JUMP) * 0.1f;
        }

        return baseHeight;
    }

    public static double getHorizontalDistance(KLocation one, KLocation two) {
        return MathUtils.hypot(one.x - two.x, one.z - two.z);
    }

    public static float getBaseSpeed(ObjectData data) {
        return 0.25f + (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED) * (data.playerInfo.serverGround ? 0.048f : 0.03f)) + (Math.max(0, data.getPlayer().getWalkSpeed() - 0.2f) * 1.6f);
    }

    public static float getFriction(ObjectData data) {
        float friction = 0.6f;

        if(data.blockInfo.onSlime) {
            friction = 0.8f;
        } else if(data.blockInfo.onIce) {
            friction = 0.98f;
        }
        return friction;
    }
}
