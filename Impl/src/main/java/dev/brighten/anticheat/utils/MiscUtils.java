package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.impl.CraftReflection;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import cc.funkemunky.api.tinyprotocol.packet.types.Vec3D;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.commands.KauriCommand;
import dev.brighten.anticheat.processing.MovementProcessor;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    public static double max(double... values) {
        return Arrays.stream(values).max().orElse(Double.MAX_VALUE);
    }

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static void testMessage(String message) {
        KauriCommand.getTesters().forEach(pl -> pl.sendMessage(Color.translate(message)));
    }

    public static void close(Closeable... closeables) {
        try {
            for (Closeable closeable : closeables) if (closeable != null) closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static WrappedField ticksField = MinecraftReflection.minecraftServer.getFieldByName("ticks");
    private static Object minecraftServer = null;
    public static int currentTick() {
        if(minecraftServer == null) minecraftServer = CraftReflection.getMinecraftServer();
        return ticksField.get(minecraftServer);
    }

    private static WrappedClass movingObjectPos = Reflections.getNMSClass("MovingObjectPosition");
    private static WrappedMethod calcIntercept = MinecraftReflection.axisAlignedBB
            .getMethodByType(movingObjectPos.getParent(), 0);
    private static WrappedField movingObjectPosVec3DField = movingObjectPos
            .getFieldByType(MinecraftReflection.vec3D.getParent(), 0);

    public static Vec3D calculateIntercept(BoundingBox box, Vector vecA, Vector vecB) {
        Object axisAlignedBB = box.toAxisAlignedBB();

        Object movingObjectPos = calcIntercept
                .invoke(axisAlignedBB, new Vec3D(vecA.getX(), vecA.getY(), vecA.getZ()).getObject(),
                        new Vec3D(vecB.getX(), vecB.getY(), vecB.getZ()).getObject());

        if(movingObjectPos != null) {
            return new Vec3D((Object) movingObjectPosVec3DField.get(movingObjectPos));
        }
        return null;
    }

    public static void close(AutoCloseable... closeables) {
        try {
            for (AutoCloseable closeable : closeables) if (closeable != null) closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    //Args: Tuple (a) is low outliers, Tupe (B) is high outliers
    public static Tuple<List<Double>, List<Double>> getOutliers(Collection<? extends Number> collection) {
        List<Double> values = new ArrayList<>();

        for (Number number : collection) {
            values.add(number.doubleValue());
        }

        if(values.size() < 4) return new Tuple<>(new ArrayList<>(), new ArrayList<>());

        double q1 = getMedian(values.subList(0, values.size() / 2)),
                q3 = getMedian(values.subList(values.size() / 2, values.size()));
        double iqr = Math.abs(q1 - q3);

        double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        val tuple = new Tuple<List<Double>, List<Double>>(new ArrayList<>(), new ArrayList<>());

        for (Double value : values) {
            if(value < lowThreshold) tuple.one.add(value);
            else if(value > highThreshold) tuple.two.add(value);
        }

        return tuple;
    }

    public static double getMedian(List<Double> data) {
        if(data.size() > 1) {
            if (data.size() % 2 == 0)
                return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
            else
                return data.get(Math.round(data.size() / 2f));
        }
        return 0;
    }

    public static double getMedian(Iterable<? extends Number> iterable) {
        List<Double> data = new ArrayList<>();

        for (Number number : iterable) {
            data.add(number.doubleValue());
        }

        return getMedian(data);
    }

    //Copied from apache math Kurtosis class.
    public static double getKurtosisApache(Iterable<? extends Number> iterable) {
        List<Double> values = new ArrayList<>();

        double total = 0;
        double kurt = Double.NaN;
        for (Number number : iterable) {
            double v = number.doubleValue();
            total+= v;
            values.add(v);
        }

        if(values.size() < 2) return kurt;

        double mean = total / values.size();
        double stdDev = MathUtils.stdev(values);
        double accum3 = 0.0D;

        for (Double value : values) {
            accum3 += Math.pow(value - mean, 4.0D);
        }

        accum3 /= Math.pow(stdDev, 4.0D);
        double n0 = values.size();
        double coefficientOne = n0 * (n0 + 1.0D) / ((n0 - 1.0D) * (n0 - 2.0D) * (n0 - 3.0D));
        double termTwo = 3.0D * Math.pow(n0 - 1.0D, 2.0D) / ((n0 - 2.0D) * (n0 - 3.0D));
        kurt = coefficientOne * accum3 - termTwo;

        return kurt;
    }

    public static double getKurtosis(final Iterable<? extends Number> iterable) {
        double n = 0.0;
        double n2 = 0.0;

        for (Number number : iterable) {
            n += number.doubleValue();
            ++n2;
        }

        if (n2 < 3.0) {
            return 0.0;
        }
        final double n3 = n2 * (n2 + 1.0) / ((n2 - 1.0) * (n2 - 2.0) * (n2 - 3.0));
        final double n4 = 3.0 * Math.pow(n2 - 1.0, 2.0) / ((n2 - 2.0) * (n2 - 3.0));
        final double n5 = n / n2;
        double n6 = 0.0;
        double n7 = 0.0;
        for (final Number n8 : iterable) {
            n6 += Math.pow(n5 - n8.doubleValue(), 2.0);
            n7 += Math.pow(n5 - n8.doubleValue(), 4.0);
        }
        return n3 * (n7 / Math.pow(n6 / n2, 2.0)) - n4;
    }

    public static float pow(float number, int times) {
        float answer = number;

        if(times <= 0) return 0;

        for(int i = 1 ; i < times ; i++) {
            answer*= number;
        }

        return answer;
    }

    public static double varianceSquared(final Number n, final Iterable<? extends Number> iterable) {
        double n2 = 0.0;
        int n3 = 0;

        for (Number number : iterable) {
            n2 += Math.pow((number).doubleValue() - n.doubleValue(), 2.0);
            ++n3;
        }

        return (n2 == 0.0) ? 0.0 : (n2 / (n3 - 1));
    }

    public static List<Double> getModes(final Iterable<? extends Number> iterable) {
        List<Double> numbers = new ArrayList<>();

        for (Number number : iterable) {
            numbers.add(number.doubleValue());
        }
        final Map<Double, Long> countFrequencies = numbers.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        final double maxFrequency = countFrequencies.values().stream()
                .mapToDouble(count -> count)
                .max().orElse(-1);

        return countFrequencies.entrySet().stream()
                .filter(tuple -> tuple.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    //Copied from apache math Skewness class.
    public static double getSkewnessApache(Iterable<? extends Number> iterable) {
        List<Double> values = new ArrayList<>();

        double total = 0;
        double skew = Double.NaN;
        for (Number number : iterable) {
            double v = number.doubleValue();
            total+= v;
            values.add(v);
        }

        if(values.size() < 2) return skew;

        double m = total / values.size();
        double accum = 0.0D;
        double accum2 = 0.0D;

        for (Double value : values) {
            double d = value - m;
            accum += d * d;
            accum2 += d;
        }

        double variance = (accum - accum2 * accum2 / values.size()) / (values.size() - 1);
        double accum3 = 0.0D;

        for (Double value : values) {
            double d = value - m;
            accum3 += d * d * d;
        }

        accum3 /= variance * Math.sqrt(variance);
        double n0 = values.size();
        skew = n0 / ((n0 - 1.0D) * (n0 - 2.0D)) * accum3;

        return skew;
    }

    public static double getSkewness(final Iterable<? extends Number> iterable) {
        double sum = 0;
        int buffer = 0;

        final List<Double> numberList = new ArrayList<>();

        for (Number num : iterable) {
            sum += num.doubleValue();
            buffer++;

            numberList.add(num.doubleValue());
        }

        Collections.sort(numberList);

        final double mean =  sum / buffer;
        final double median = (buffer % 2 != 0) ? numberList.get(buffer / 2) : (numberList.get((buffer - 1) / 2) + numberList.get(buffer / 2)) / 2;

        return 3 * (mean - median) / deviationSquared(iterable);
    }

    public static float stdev(Collection<Float> list) {
        float sum = 0.0f;
        float num = 0.0f;

        for (Float v : list) {
            sum+= v;
        }

        float mean = sum / (float)list.size();

        for (Float v : list) {
            num+= Math.pow(v - mean, 2.0D);
        }

        return MathHelper.sqrt(num / (float)list.size());
    }

    public static float normalizeAngle(float angle) {
        while (angle > 360.0F)
            angle -= 360.0F;
        while (angle < 0.0F)
            angle += 360.0F;
        return angle;
    }

    public static double deviationSquared(final Iterable<? extends Number> iterable) {
        double n = 0.0;
        int n2 = 0;

        for (Number anIterable : iterable) {
            n += (anIterable).doubleValue();
            ++n2;
        }
        final double n3 = n / n2;
        double n4 = 0.0;

        for (Number anIterable : iterable) {
            n4 += Math.pow(anIterable.doubleValue() - n3, 2.0);
        }

        return (n4 == 0.0) ? 0.0 : (n4 / (n2 - 1));
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

    public static int gcd(int current, int previous) {
        return (Math.abs(previous) <= 16384L) ? Math.abs(current) : gcd(previous, current % previous);
    }

    public static float gcd(float current, float previous) {
        val sens = MovementProcessor.getSensitivityFromYawGCD(current / MovementProcessor.offset);

        return sens >= 0 || sens <= 1 ? Math.abs(current) : gcd(previous, current % previous);
    }

    public static int getDecimalCount(float number) {
        return String.valueOf(number).split("\\.")[1].length();
    }

    public static int getDecimalCount(double number) {
        return String.valueOf(number).split("\\.")[1].length();
    }

    public static long gcd(long current, long previous) {
        return (previous <= 16384L) ? current : gcd(previous, current % previous);
    }

    public static long gcdPrevious(long current, long previous) {
        return (previous <= 16384L) ? previous : gcdPrevious(previous, current % previous);
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
