package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedMethod;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckSettings;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CheckManager {
    private ObjectData objectData;
    public Map<String, Check> checks = new HashMap<>();
    public Map<Class<?>, List<Map.Entry<String, WrappedMethod>>> checkMethods = new ConcurrentHashMap<>();

    public CheckManager(ObjectData objectData) {
        this.objectData = objectData;
    }

    public void runPacket(NMSObject object, long timeStamp) {
        if(!checkMethods.containsKey(object.getClass())) return;
        checkMethods.get(object.getClass()).parallelStream().forEach(entry -> {
            Check check = checks.get(entry.getKey());
            Kauri.INSTANCE.profiler.start("check:" + check.name);

            if(check.enabled) {
                try {
                    if(entry.getValue().getMethod().getParameterCount() > 1) {
                        entry.getValue().getMethod().invoke(check, object, timeStamp);
                    } else {
                        entry.getValue().getMethod().invoke(check, object);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.out.println("Error on " + check.name);
                    e.printStackTrace();
                }
            }
            Kauri.INSTANCE.profiler.stop("check:" + check.name);
        });
    }

    public void addChecks() {
        Check.checkClasses.keySet().parallelStream()
                .map(clazz -> {
                    CheckInfo settings = Check.checkClasses.get(clazz);
                    Check check = clazz.getConstructor().newInstance();
                    check.data = objectData.INSTANCE;
                    CheckSettings checkSettings = Check.checkSettings.get(clazz);
                    check.enabled = checkSettings.enabled;
                    check.executable = checkSettings.executable;
                    check.developer = settings.developer();
                    check.name = settings.name();
                    check.description = settings.description();
                    return check;
                })
                .sequential()
                .forEach(check -> checks.put(check.name, check));

        for (String name : checks.keySet()) {
            Check check = checks.get(name);
            WrappedClass checkClass = new WrappedClass(check.getClass());
            System.out.println("Added check: " + name);
            Arrays.stream(check.getClass().getDeclaredMethods())
                    .parallel()
                    .filter(method -> method.isAnnotationPresent(Packet.class))
                    .map(method -> new WrappedMethod(checkClass, method))
                    .forEach(method -> {
                        Class<?> parameter = method.getParameters().get(0);
                        List<Map.Entry<String, WrappedMethod>> methods = checkMethods.getOrDefault(
                                parameter,
                                new ArrayList<>());

                        methods.add(new AbstractMap.SimpleEntry<>(
                                check.name, method));

                        System.out.println("added packet method: " + method.getName());
                        checkMethods.put(parameter, methods);
                    });
        }
    }
}
