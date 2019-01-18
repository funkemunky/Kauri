package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.ReflectionsUtil;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class MiscUtils {

    public static boolean shouldReturnArmAnimation(PlayerData data) {
        return data.isBreakingBlock() || data.getLastBlockPlace().hasNotPassed(2);
    }

    public static float convertToMouseDelta(float value) {
        return ((float) Math.cbrt((value / 0.15f) / 8f) - 0.2f) / .6f;
    }

    public static float getDistanceToGround(PlayerData data, float max) {
        BoundingBox toCheck = data.getBoundingBox().subtract(0, max, 0, 0, 0, 0);

        List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(data.getPlayer().getWorld(), toCheck);

        BoundingBox highestBox = boxes.stream().min(Comparator.comparingDouble(box -> 500 - box.minY)).orElse(new BoundingBox(data.getMovementProcessor().getTo().toVector(), data.getMovementProcessor().getTo().toVector()));

        return data.getBoundingBox().minY - highestBox.maxY;
    }

    public static void allahAkbar(Player player) {
        Object packet = ReflectionsUtil.newInstance(ReflectionsUtil.getNMSClass("PacketPlayOutExplosion"), player.getEyeLocation().getX(), player.getEyeLocation().getY(), player.getEyeLocation().getZ(), 20, Lists.newArrayList(), null);

        ReflectionsUtil.getMethodValue(ReflectionsUtil.getMethod(ReflectionsUtil.getNMSClass("EntityPlayer"), "sendPacket", Object.class), ReflectionsUtil.getEntityPlayer(player), packet);
    }

    public static float wrapAngleTo180_float(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

}
