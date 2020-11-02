package dev.brighten.anticheat.utils;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;

public class ReflectionUtil {

    private static final WrappedClass entityTracker = Reflections.getNMSClass("EntityTracker"),
            entityTrackerEntry = Reflections.getNMSClass("EntityTrackerEntry"),
            intHashMap = Reflections.getNMSClass("IntHashMap");
    private static final WrappedField worldET = MinecraftReflection.worldServer
            .getFieldByType(entityTracker.getParent(), 0),
            etMap = entityTracker.getFieldByType(intHashMap.getParent(), 0),
            fieldXLoc = entityTrackerEntry.getFieldByName("xLoc"),
            fieldYLoc = entityTrackerEntry.getFieldByName("yLoc"),
            fieldZLoc = entityTrackerEntry.getFieldByName("zLoc");
    private static final WrappedMethod ihmGet = intHashMap.getMethod("get", int.class);


    public static <T> T getEntityTracker(Object worldServer) {
        return worldET.get(worldServer);
    }

    public static <T> T getTrackerEntityMap(Object entityTracker) {
        return etMap.get(entityTracker);
    }

    public static <T> T getEntityById(Object worldSever, int id) {
        Object tracker = getEntityTracker(worldSever);
        Object map = getTrackerEntityMap(tracker);

        return ihmGet.invoke(map, id);
    }

    public static int[] getTrackerLoc(Object worldServer, int id) {
        Object entity = getEntityById(worldServer, id);

        return new int[]{fieldXLoc.get(entity), fieldYLoc.get(entity), fieldZLoc.get(entity)};
    }
}
