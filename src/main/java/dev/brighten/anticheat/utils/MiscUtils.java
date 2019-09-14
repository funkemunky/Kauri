package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MiscUtils {

    public static double EXPANDER = Math.pow((double)2.0, (double)24.0);
    private final static Set<Class<?>> NUMBER_REFLECTED_PRIMITIVES;
    static {
        Set<Class<?>> s = new HashSet<>();
        s.add(byte.class);
        s.add(short.class);
        s.add(int.class);
        s.add(long.class);
        s.add(float.class);
        s.add(double.class);
        NUMBER_REFLECTED_PRIMITIVES = s;
    }

    public static float getYawChangeToEntity(Player player, LivingEntity entity, KLocation from, KLocation to) {
        double deltaX = entity.getLocation().getX() - player.getLocation().getX();
        double deltaZ = entity.getLocation().getZ() - player.getLocation().getZ();
        double yawToEntity;
        if(deltaZ < 0.0D && deltaX < 0.0D) {
            yawToEntity = 90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX));
        } else if(deltaZ < 0.0D && deltaX > 0.0D) {
            yawToEntity = -90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX));
        } else {
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        return -MathUtils.getAngleDelta(from.yaw, to.yaw) - (float)yawToEntity;
    }

    public static float getPitchChangeToEntity(Player player, LivingEntity entity, KLocation from, KLocation to) {
        double deltaX = entity.getLocation().getX() - player.getLocation().getX();
        double deltaZ = entity.getLocation().getZ() - player.getLocation().getZ();
        double deltaY = player.getLocation().getY() - 1.6D + 2.0D - 0.4D - entity.getLocation().getY();
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));
        return MathUtils.yawTo180F((float)(-(MathUtils.getAngleDelta(from.yaw, to.yaw) - (double)((float)pitchToEntity))));
    }


    public static boolean isReflectedAsNumber(Class<?> type) {
        return Number.class.isAssignableFrom(type) || NUMBER_REFLECTED_PRIMITIVES.contains(type);
    }

    public static long gcd(long current, long previous) {
        return (previous <= 16384L) ? current : gcd(previous, current % previous);
    }

    public static float gcd(float current, float previous) {
        return (previous <= 16384L) ? current : gcd(previous, current % previous);
    }
}
