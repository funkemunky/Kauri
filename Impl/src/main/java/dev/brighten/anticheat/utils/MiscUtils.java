package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumAnimation;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.commands.KauriCommand;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
            players.forEach(p -> TinyProtocolHandler.sendPacket(p, packet));
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

    //Skidded from Luke.
    public static double getAngle(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return -1;
        Vector playerRotation = new Vector(loc1.getYaw(), loc1.getPitch(), 0.0f);
        loc1.setY(0);
        loc2.setY(0);
        val rot = MathUtils.getRotations(loc1, loc2);
        Vector expectedRotation = new Vector(rot[0], rot[1], 0);
        return MathUtils.yawTo180D(playerRotation.getX() - expectedRotation.getX());
    }

    /* Stolen from Bukkit */
    public static Vector getDirection(KLocation loc) {
        Vector vector = new Vector();
        double rotX = (double)loc.yaw;
        double rotY = (double)loc.pitch;
        vector.setY(-Math.sin(Math.toRadians(rotY)));
        double xz = Math.cos(Math.toRadians(rotY));
        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
        return vector;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    public static LongStream listToStream(Collection<Long> collection) {
        LongStream.Builder longBuilder = LongStream.builder();
        collection.forEach(longBuilder::add);
        return longBuilder.build();
    }

    private static final int[] decimalPlaces = {0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    public static double format(double d, int dec) {
        return (long) (d * decimalPlaces[dec] + 0.5) / (double) decimalPlaces[dec];
    }

    public static String drawUsage(long max, long time) {
        double chunk = max / 50;
        String line = IntStream.range(0, 50).mapToObj(i -> (chunk * i < time ? "§c" : "§7") + "❘").collect(Collectors.joining("", "[", ""));
        String zeros = "00";
        String nums = Integer.toString((int) ((time / (double) max) * 100));
        return line + "§f] §c" + zeros.substring(0, 3 - nums.length()) + nums + "% §f❘";
    }

    public static String drawUsage(long max, double time) {
        double chunk = max / 50;
        String line = IntStream.range(0, 50).mapToObj(i -> (chunk * i < time ? "§c" : "§7") + "❘").collect(Collectors.joining("", "[", ""));
        String nums = String.valueOf(format((time / (double) max) * 100, 3));
        return line + "§f] §c" + nums + "%";
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

    public static long lcm(long a, long b)
    {
        return a * (b / gcd(a, b));
    }

    public static long lcm(long[] input)
    {
        long result = input[0];
        for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
        return result;
    }

    public static String timeStampToDate(long timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY (hh:mm)");

        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = new Date(timeStamp);

        return format.format(date);
    }
}
