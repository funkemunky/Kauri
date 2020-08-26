package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class MovementUtils {

    private static Enchantment DEPTH;

    public static float getJumpHeight(ObjectData data) {
        float baseHeight = 0.42f;

        if(data.potionProcessor.hasPotionEffect(PotionEffectType.JUMP)) {
            baseHeight+= PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP) * 0.1f;
        }

        return baseHeight;
    }

    public static boolean isOnLadder(ObjectData data) {
        try {
            int i = MathHelper.floor_double(data.playerInfo.to.x);
            int j = MathHelper.floor_double(data.box.yMin);
            int k = MathHelper.floor_double(data.playerInfo.to.z);
            Block block = BlockUtils.getBlock(new Location(data.getPlayer().getWorld(), i, j, k));

            return Materials.checkFlag(block.getType(), Materials.LADDER);
        } catch(NullPointerException e) {
            return false;
        }
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
        return 0.2806 + (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED)
                * (data.playerInfo.clientGround ? 0.062 : 0.04)) + (data.getPlayer().getWalkSpeed() - 0.2) * 2.5;
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
        return getTotalHeight(ProtocolVersion.V1_8_9, initial);
    }

    public static float getTotalHeight(ProtocolVersion version, float initial) {
        float nextCalc = initial, total = initial;
        int count = 0;
        while ((nextCalc = (nextCalc - 0.08f) * 0.98f) > (version.isOrBelow(ProtocolVersion.V1_8_9) ?  0.005 : 0)) {
            total+= nextCalc;
            if(count++ > 15) {
                return total * 4;
            }
        }

        return total;
    }

    static {
        try {
            if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_8)) {
                DEPTH = Enchantment.getByName("DEPTH_STRIDER");
            }
        } catch(Exception e) {
            DEPTH = null;
        }
    }
}
