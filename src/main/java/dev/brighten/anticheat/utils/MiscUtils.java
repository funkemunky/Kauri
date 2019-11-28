package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumAnimation;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.commands.KauriCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.LongStream;

public class MiscUtils {

    private final static Set<Class<?>> NUMBER_REFLECTED_PRIMITIVES;

    public static String deltaSymbol = "\u0394";

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

    public static void drawRay(RayCollision collision, WrappedEnumParticle particle, Collection<? extends Player> players) {
        for (double i = 0; i < 8; i += 0.2) {
            float fx = (float) (collision.originX + (collision.directionX * i));
            float fy = (float) (collision.originY + (collision.directionY * i));
            float fz = (float) (collision.originZ + (collision.directionZ * i));
            Object packet = new WrappedPacketPlayOutWorldParticle(particle, true, fx, fy, fz,
                    0F, 0F, 0F, 0, 0).getObject();
            for (Player p : players) TinyProtocolHandler.sendPacket(p, packet);
        }
    }

    public static void testMessage(String message) {
        KauriCommand.getTesters().forEach(pl -> pl.sendMessage(Color.translate(message)));
    }

    public static boolean isAnimated(HumanEntity entity) {
        Object itemInUse = MinecraftReflection.getItemInUse(entity);

        if(itemInUse == null) return false;

        Object animation = MinecraftReflection.getItemAnimation(itemInUse);

        return !WrappedEnumAnimation.fromNMS(animation).equals(WrappedEnumAnimation.NONE);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static LongStream listToStream(Collection<Long> collection) {
        LongStream.Builder longBuilder = LongStream.builder();
        for (Long aLong : collection) {
            longBuilder.add(aLong);
        }
        return longBuilder.build();
    }

    private static final int[] decimalPlaces = {0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    public static double format(double d, int dec) {
        return (long) (d * decimalPlaces[dec] + 0.5) / (double) decimalPlaces[dec];
    }

    public static String drawUsage(long max, long time) {
        double chunk = max / 50;
        StringBuilder line = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            line.append((chunk * i < time ? "§c" : "§7") + "❘");
        }
        String zeros = "00";
        String nums = Integer.toString((int) ((time / (double) max) * 100));
        return line.toString() + "§f] §c" + zeros.substring(0, 3 - nums.length()) + nums + "% §f❘";
    }

    public static String drawUsage(long max, double time) {
        double chunk = max / 50;
        StringBuilder line = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            line.append((chunk * i < time ? "§c" : "§7") + "❘");
        }
        String nums = String.valueOf(format((time / (double) max) * 100, 3));
        return line.toString() + "§f] §c" + nums + "%";
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

    public static String timeStampToDate(long timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY (hh:mm)");

        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = new Date(timeStamp);

        return format.format(date);
    }
}
