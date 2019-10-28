package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedMethod;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckSettings;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.event.Event;

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

        val methods = checkMethods.get(object.getClass());
        methods.parallelStream().filter(entry -> entry.getValue().getMethod().isAnnotationPresent(Packet.class))
                .forEach(entry -> {
                    Check check = checks.get(entry.getKey());

                    if(check.enabled) {
                        if(entry.getValue().getMethod().getParameterCount() > 1) {
                            entry.getValue().invoke(check, object, timeStamp);
                        } else {
                            entry.getValue().invoke(check, object);
                        }
                    }
                });
    }

    public void runEvent(Event event) {
        if(!checkMethods.containsKey(event.getClass())) return;

        val methods = checkMethods.get(event.getClass());

        methods.parallelStream().filter(entry ->
                entry.getValue().getMethod().isAnnotationPresent(dev.brighten.anticheat.check.api.Event.class))
                .forEach(entry -> {
                    Check check = checks.get(entry.getKey());

                    if(check.enabled) {
                        entry.getValue().invoke(check, event);
                    }
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
                    check.punishVl = settings.punishVL();
                    check.checkType = settings.checkType();
                    return check;
                })
                .sequential()
                .forEach(check -> checks.put(check.name, check));

        for (String name : checks.keySet()) {
            Check check = checks.get(name);
            WrappedClass checkClass = new WrappedClass(check.getClass());

            Arrays.stream(check.getClass().getDeclaredMethods())
                    .parallel()
                    .filter(method -> method.isAnnotationPresent(Packet.class)
                            || method.isAnnotationPresent(dev.brighten.anticheat.check.api.Event.class))
                    .map(method -> new WrappedMethod(checkClass, method))
                    .forEach(method -> {
                        Class<?> parameter = method.getParameters().get(0);
                        List<Map.Entry<String, WrappedMethod>> methods = checkMethods.getOrDefault(
                                parameter,
                                new ArrayList<>());

                        methods.add(new AbstractMap.SimpleEntry<>(
                                check.name, method));
                        checkMethods.put(parameter, methods);
                    });
        }
    }
}
