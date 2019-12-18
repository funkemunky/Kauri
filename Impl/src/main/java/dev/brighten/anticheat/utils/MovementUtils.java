package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
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
        return 0.25f + (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED) * (data.playerInfo.serverGround ? 0.06f : 0.04f)) + (Math.max(0, data.getPlayer().getWalkSpeed() - 0.2f) * 1.6f);
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

    public static float getTotalHeight(Player player, float initial) {
        float total = initial;
        float nextCalc = initial;
        int test = 0;
        while (((nextCalc - 0.08f) * 0.98f) > 0) {
            float calc = nextCalc - 0.08f;
            calc*= 0.98f;
            total+= calc;
            nextCalc = calc;
            if(test++ > 15) {
                return total * 4;
            }
        }

        return total;
    }
}
