package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class MovementUtils {

    private static Enchantment DEPTH;

    public static float getJumpHeight(ObjectData data) {
        float baseHeight = 0.42f;

        baseHeight+= data.potionProcessor.getEffectByType(PotionEffectType.JUMP).map(ef -> ef.getAmplifier() + 1)
                .orElse(0) * 0.1f;

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

    private static final WrappedField checkMovement = ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)
            ? MinecraftReflection.playerConnection.getFieldByName("checkMovement")
            : MinecraftReflection.playerConnection.getFieldByName(ProtocolVersion.getGameVersion()
            .isOrAbove(ProtocolVersion.v1_17) ? "y" : "teleportPos");
    public static boolean checkMovement(Player player) {
        Object playerConnection = MinecraftReflection.getPlayerConnection(player);

        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
            return checkMovement.get(playerConnection);
        } else return (checkMovement.get(playerConnection) == null);
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

    public static float getFriction(Block block) {
        Optional<XMaterial> matched = XMaterial.matchXMaterial(block.getType().name());

        if(!matched.isPresent()) return 0.6f;
        switch(matched.get()) {
            case SLIME_BLOCK:
                return 0.8f;
            case ICE:
            case BLUE_ICE:
            case FROSTED_ICE:
            case PACKED_ICE:
                return 0.98f;
            default:
                return 0.6f;
        }
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
