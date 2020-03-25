package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.event.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheckManager {
    private ObjectData objectData;
    public Map<String, Check> checks = new HashMap<>();
    private Map<Class<?>, List<WrappedCheck>> checkMethods = new HashMap<>();

    public CheckManager(ObjectData objectData) {
        this.objectData = objectData;
    }

    public boolean runPacket(NMSObject object, long timeStamp) {
        if(!checkMethods.containsKey(object.getClass())) return true;

        val methods = checkMethods.get(object.getClass());
        AtomicBoolean okay = new AtomicBoolean(true);
        methods.parallelStream()
                .forEach(wrapped -> {
                    if(wrapped.isPacket && wrapped.check.enabled && wrapped.isCompatible()) {
                        if(wrapped.isBoolean) {
                            if(wrapped.oneParam) {
                                boolean returned = wrapped.method.invoke(wrapped.check, object);

                                if(!returned) okay.set(false);
                            }
                            else {
                                boolean returned = wrapped.method.invoke(wrapped.check, object, timeStamp);

                                if(!returned) okay.set(false);
                            }
                        } else {
                            if(wrapped.oneParam) wrapped.method.invoke(wrapped.check, object);
                            else wrapped.method.invoke(wrapped.check, object, timeStamp);
                        }
                    }
                });
        return okay.get();
    }

    public void runEvent(Event event) {
        if(!checkMethods.containsKey(event.getClass())) return;

        val methods = checkMethods.get(event.getClass());

        methods.parallelStream()
                .forEach(wrapped -> {
                    if(wrapped.isPacket && wrapped.check.enabled) {
                        wrapped.method.invoke(wrapped.check, event);
                    }
                });
    }

    public void runEvent(AtlasEvent event) {
        if(!checkMethods.containsKey(event.getClass())) return;

        val methods = checkMethods.get(event.getClass());

        methods.parallelStream()
                .forEach(wrapped -> {
                    if(wrapped.isPacket && wrapped.check.enabled) {
                        wrapped.method.invoke(wrapped.check, event);
                    }
                });
    }

    public void addChecks() {
        if(objectData.getPlayer().hasPermission("kauri.bypass") && Config.bypassPermission) return;
        Check.checkClasses.keySet().stream()
                .map(clazz -> {
                    CheckInfo settings = Check.checkClasses.get(clazz);
                    Check check = clazz.getConstructor().newInstance();
                    check.data = objectData.INSTANCE;
                    CheckSettings checkSettings = Check.checkSettings.get(clazz);
                    check.enabled = checkSettings.enabled;
                    check.executable = checkSettings.executable;
                    check.cancellable = checkSettings.cancellable;
                    check.cancelMode = checkSettings.cancelMode;
                    check.developer = settings.developer();
                    check.name = settings.name();
                    check.description = settings.description();
                    check.punishVl = settings.punishVL();
                    check.checkType = settings.checkType();
                    check.maxVersion = settings.maxVersion();
                    check.vlToFlag = settings.vlToFlag();
                    check.minVersion = settings.minVersion();
                    check.banExempt = objectData.getPlayer().hasPermission("kauri.bypass.ban");
                    return check;
                })
                .sequential()
                .forEach(check -> checks.put(check.name, check));

        checks.keySet().parallelStream().map(name -> checks.get(name)).forEach(check -> {
            WrappedClass checkClass = new WrappedClass(check.getClass());
            
            Arrays.stream(check.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Packet.class)
                            || method.isAnnotationPresent(dev.brighten.anticheat.check.api.Event.class))
                    .map(method -> new WrappedMethod(checkClass, method))
                    .forEach(method -> {
                        Class<?> parameter = method.getParameters().get(0);
                        List<WrappedCheck> methods = checkMethods.getOrDefault(
                                parameter,
                                new ArrayList<>());

                        methods.add(new WrappedCheck(check, method));
                        checkMethods.put(parameter, methods);
                    });
        });
    }
}
