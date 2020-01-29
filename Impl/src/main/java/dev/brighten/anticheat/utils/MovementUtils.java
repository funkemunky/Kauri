package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class MovementUtils {

    private static Enchantment DEPTH;

    public static float getJumpHeight(Player player) {
        float baseHeight = 0.42f;

        if(player.hasPotionEffect(PotionEffectType.JUMP)) {
            baseHeight+= PlayerUtils.getPotionEffectLevel(player, PotionEffectType.JUMP) * 0.1f;
        }

        return baseHeight;
    }

    public static int getDepthStriderLevel(Player player) {
        if(DEPTH == null) return 0;

        val boots = player.getInventory().getBoots();

        if(boots == null) return 0;

        return boots.getEnchantmentLevel(DEPTH);
    }

    public static double getHorizontalDistance(KLocation one, KLocation two) {
        return MathUtils.hypot(one.x - two.x, one.z - two.z);
    }

    public static double getBaseSpeed(ObjectData data) {
        return (0.284 + PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED)
                * (data.playerInfo.clientGround ? 0.052 : 0.028));
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

    public static float getTotalHeight(float initial) {
        float nextCalc = initial, total = initial;
        int count = 0;
        while ((nextCalc = (nextCalc - 0.08f) * 0.98f) > 0.005) {
            total+= nextCalc;
            if(count++ > 15) {
                return total * 4;
            }
        }

        return total;
    }

    static {
        try {
            DEPTH = Enchantment.getByName("DEPTH_STRIDER");
        } catch(Exception e) {
            DEPTH = null;
        }
    }
}
