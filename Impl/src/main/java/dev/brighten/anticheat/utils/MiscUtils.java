package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflections.impl.CraftReflection;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.commands.KauriCommand;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.processing.MovementProcessor;
import lombok.val;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
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

    public static void forceBanPlayer(ObjectData data) {
        assert data.checkManager.checks.containsKey("ForceBan") : "Player " + data.getPlayer().getName()
                + " does not have ForceBan detection loaded; cannot force ban.";

        Check ban = data.checkManager.checks.get("ForceBan");

        ban.vl = 2;
        ban.punish();
    }

    public static float clampToVanilla(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }

    public static boolean endsWith(double value, String string) {
        return String.valueOf(value).endsWith(string);
    }

    public static Float getMode(Collection<Float> collect) {
        Map<Float, Integer> repeated = new HashMap<>();

        //Sorting each value by how to repeat into a map.
        collect.forEach(val -> {
            float value = (float)MathUtils.trim(7, val);
            int number = repeated.getOrDefault(value, 0);

            repeated.put(val, number + 1);
        });

        //Calculating the largest value to the key, which would be the mode.
        return repeated.keySet().stream()
                .map(key -> new Tuple<>(key, repeated.get(key))) //We map it into a Tuple for easier sorting.
                .max(Comparator.comparing(tup -> tup.two, Comparator.naturalOrder()))
                .orElseThrow(NullPointerException::new).one;
    }

    public static void sendMessage(CommandSender player, String message, Object... objects) {
        String toSend = String.format(Color.translate(message), objects);
        if(player instanceof Player) {
            ((Player)player).spigot().sendMessage(TextComponent.fromLegacyText(toSend));
        } else player.sendMessage(toSend);
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

    public static int length(double value) {
        return String.valueOf(value).length();
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
    //TODO Make this use the new abstraction system.
    public static int currentTick() {
        if(minecraftServer == null) minecraftServer = CraftReflection.getMinecraftServer();
        return ticksField.get(minecraftServer);
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

    public static float distanceBetweenAngles(float a, float b) {
        final float first = a % 360;
        final float second = b % 360;

        final float delta = Math.abs(first - second);

        return (float) Math.abs(Math.min(360.0 - delta, delta));
    }

    public static float getDistanceBetweenAngles(final float angle1, final float angle2) {
        float distance = Math.abs(angle1 - angle2) % 360.0f;
        if (distance > 180.0f) {
            distance = 360.0f - distance;
        }
        return distance;
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

    /** Nik's method **/
    public static <E> E randomElement(final Collection<? extends E> collection) {
        if (collection.size() == 0) return null;
        int index = new Random().nextInt(collection.size());

        if (collection instanceof List) {
            return ((List<? extends E>) collection).get(index);
        } else {
            Iterator<? extends E> iter = collection.iterator();
            for (int i = 0; i < index; i++) iter.next();
            return iter.next();
        }
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

    public static Tuple<List<Long>, List<Long>> getOutliersLong(Collection<Long> collection) {
        List<Long> values = new ArrayList<>(collection);

        if(values.size() < 4) return new Tuple<>(new ArrayList<>(), new ArrayList<>());

        double q1 = getMedian(values.subList(0, values.size() / 2)),
                q3 = getMedian(values.subList(values.size() / 2, values.size()));
        double iqr = Math.abs(q1 - q3);

        double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        val tuple = new Tuple<List<Long>, List<Long>>(new ArrayList<>(), new ArrayList<>());

        for (Long value : values) {
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

    public static double stdev(final Iterable<? extends Number> iterable) {
        double sum = 0.0f;
        double num = 0.0f;

        final List<Double> list = new ArrayList<>();

        for (Number number : iterable) {
            list.add(number.doubleValue());
        }

        for (Double v : list) {
            sum+= v;
        }

        double mean = sum / (float)list.size();

        for (Double v : list) {
            num+= Math.pow(v - mean, 2.0D);
        }

        return MathHelper.sqrt(num / (double)list.size());
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
