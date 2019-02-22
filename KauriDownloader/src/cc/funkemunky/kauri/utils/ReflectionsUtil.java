package cc.funkemunky.kauri.utils;

import cc.funkemunky.kauri.KauriDownloader;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionsUtil {

    public static String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    public static File getPluginFolder() {
        Object console = getMethodValue(getMethod(getCBClass("CraftServer"), "console"), KauriDownloader.getInstance().getServer());
        Object options = getFieldValue(getFieldByName(getNMSClass("MinecraftServer"), "options"), console);
        return (File) getMethodValue(getMethod(getNMSClass("OptionSet"), "valueOf", String.class), options, "plugins");
    }

    public static Field getFirstFieldByType(Class<?> clazz, Class<?> type) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().equals(type)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = clazz.getMethod(methodName, args);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Method getMethodNoST(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = clazz.getMethod(methodName, args);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean hasMethod(Class clazz, Method method) {
        return Arrays.stream(clazz.getMethods()).anyMatch(methodLoop -> methodLoop.getName().equals(method.getName()));
    }

    public static boolean hasMethod(Class clazz, String methodName) {
        return Arrays.stream(clazz.getMethods()).anyMatch(methodLoop -> methodLoop.getName().equals(methodName));
    }

    public static Object getMethodValue(Method method, Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasField(Class<?> object, String fieldName) {
        return Arrays.stream(object.getFields()).anyMatch(field -> field.getName().equalsIgnoreCase(fieldName));
    }

    public static Object getMethodValueNoST(Method method, Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getFieldValue(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getFieldValueNoST(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            return null;
        }
    }

    public static Field getFieldByNameNoST(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName) != null ? clazz.getDeclaredField(fieldName) : clazz.getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object newInstance(Class<?> objectClass, Object... args) {
        try {
            return objectClass.getConstructor(args.getClass()).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getNMSClass(String string) {
        return getClass("net.minecraft.server." + version + "." + string);
    }

    public static Class<?> getCBClass(String string) {
        return getClass("org.bukkit.craftbukkit." + version + "." + string);
    }

    @SuppressWarnings("unchecked")
    public static Enum<?> getEnum(Class<?> clazz, String enumName) {
        return Enum.valueOf((Class<Enum>) clazz, enumName);
    }

    public static Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName) != null ? clazz.getDeclaredField(fieldName) : clazz.getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object setFieldValue(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return field.getDeclaringClass();
    }

    public static Class<?> getClass(String string) {
        try {
            return Class.forName(string);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
