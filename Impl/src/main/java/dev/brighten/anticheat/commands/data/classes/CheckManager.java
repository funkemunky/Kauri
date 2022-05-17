package dev.brighten.anticheat.commands.data.classes;

import cc.funkemunky.api.events.AtlasEvent;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.commands.data.ObjectData;
import lombok.val;
import org.bukkit.event.Event;

import java.util.*;

public class CheckManager {
    private ObjectData objectData;
    /**
     * Stores checkName, Check
     */
    public final Map<String, Check> checks = new HashMap<>();
    public final Map<Class<?>, List<WrappedCheck>> checkMethods = new HashMap<>();


    public CheckManager(ObjectData objectData) {
        this.objectData = objectData;
    }

    //TODO Use https://github.com/EsotericSoftware/reflectasm

    /**
     *
     * @param object (packet wrapper) NMSObject
     * @param timeStamp Long time of packet received
     */
    public void runPacket(NMSObject object, long timeStamp) {
        if(objectData.bypassing) return;
        val methods = checkMethods.get(object.getClass());

        if(methods == null) return;

        int currentTick = Kauri.INSTANCE.currentTick;
        for (WrappedCheck wrapped : methods) {
            try {
                if(!wrapped.isBoolean && wrapped.isPacket && wrapped.check.enabled && wrapped.isCompatible()) {
                    if(wrapped.oneParam) wrapped.access.invoke(wrapped.check, wrapped.methodIndex, object);
                    else {
                        if(wrapped.isTimeStamp) {
                            wrapped.access.invoke(wrapped.check, wrapped.methodIndex, object, timeStamp);
                        } else wrapped.access.invoke(wrapped.check, wrapped.methodIndex, object, currentTick);
                    }
                }
            } catch(Exception e) {
                cc.funkemunky.api.utils.MiscUtils
                        .printToConsole("Error occurred in check " + wrapped.checkName);
                e.printStackTrace();
            }
        }
    }

    public boolean runPacketCancellable(NMSObject object, long timeStamp) {
        if(objectData.bypassing) return false;
        val methods = checkMethods.get(object.getClass());

        if(methods == null) return false;

        int currentTick = Kauri.INSTANCE.currentTick;
        boolean cancelled = false;
        for (WrappedCheck wrapped : methods) {
            if(!wrapped.isBoolean) continue;
            try {
                if(wrapped.isPacket && wrapped.check.enabled && wrapped.isCompatible()) {
                    if(wrapped.oneParam) {
                        if((boolean)wrapped.access.invoke(wrapped.check, wrapped.methodIndex, object)) cancelled = true;
                    } else if(wrapped.isTimeStamp) {
                        if((boolean)wrapped.access.invoke(wrapped.check, wrapped.methodIndex, object, timeStamp))
                            cancelled = true;
                    } else if((boolean)wrapped.access.invoke(wrapped.check, wrapped.methodIndex, object, currentTick))
                        cancelled = true;
                }
            } catch(Exception e) {
                cc.funkemunky.api.utils.MiscUtils.printToConsole("Error occurred in check " + wrapped.checkName);
                e.printStackTrace();
            }
        }

        return cancelled;
    }

    public void runEvent(Event event) {
        if(objectData.bypassing) return;
        synchronized (checkMethods) {
            val methods = checkMethods.get(event.getClass());

            if(methods == null) return;

            for (WrappedCheck wrapped : methods) {
                if(wrapped.isEvent && wrapped.check.enabled) {
                    try {
                        wrapped.access.invoke(wrapped.check, wrapped.methodIndex, event);
                    } catch(Exception e) {
                        cc.funkemunky.api.utils.MiscUtils
                                .printToConsole("Error occurred in check " + wrapped.checkName);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean runEvent(Object event) {
        if(objectData.bypassing) return false;
        val methods = checkMethods.get(event.getClass());

        if(methods == null) return false;

        boolean cancelled = false;
        for (WrappedCheck wrapped : methods) {
            if(!wrapped.isPacket && wrapped.check.enabled) {
                if(!wrapped.isBoolean) {
                    wrapped.access.invoke(wrapped.check, wrapped.methodIndex, event);
                } else if((boolean)wrapped.access.invoke(wrapped.check, wrapped.methodIndex, event)) cancelled = true;
            }
        }
        return cancelled;
    }

    public void runEvent(AtlasEvent event) {
        if(objectData.bypassing || !checkMethods.containsKey(event.getClass())) return;

        val methods = checkMethods.get(event.getClass());

        for (WrappedCheck wrapped : methods) {
            if(!wrapped.isPacket && wrapped.check.enabled) {
                wrapped.access.invoke(wrapped.check, wrapped.methodIndex, event);
            }
        }
    }

    public void addChecks() {
        assert objectData != null: "ObjectData is null in CheckManager";
        if(objectData.getPlayer()
                .hasPermission("kauri.bypass")
                && Config.flagBypassPerm) return;

        //Whitelisting players with prefix for bedrock users
        if(!Config.prefixWhitelist.equals("disabled")
                && objectData.getPlayer().getName().startsWith(Config.prefixWhitelist)) return;

        synchronized (checks) {
            for (Map.Entry<WrappedClass, CheckInfo> entry
                    : Check.checkClasses.entrySet()) {
                WrappedClass clazz = entry.getKey();
                CheckInfo settings = entry.getValue();

                Check check = clazz.getConstructor().newInstance();
                check.setData(objectData);
                CheckSettings checkSettings = Check.checkSettings.get(clazz);
                check.enabled = checkSettings.enabled;
                check.executable = checkSettings.executable;
                check.cancellable = checkSettings.cancellable;
                check.cancelMode = checkSettings.cancelMode;
                check.executableCommands = checkSettings.executableCommands;
                check.devStage = settings.devStage();
                check.name = settings.name();
                check.description = settings.description();
                check.punishVl = settings.punishVL();
                check.checkType = settings.checkType();
                check.maxVersion = settings.maxVersion();
                check.vlToFlag = settings.vlToFlag();
                check.minVersion = settings.minVersion();
                check.banExempt = objectData.getPlayer().hasPermission("kauri.bypass.ban");

                checks.put(check.name, check);
            }
        }

        synchronized (checkMethods) {
            checks.values().forEach(check -> {
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
                            if(method.getMethod().isAnnotationPresent(Packet.class)) {
                                methods.sort(Comparator.comparing(m ->
                                        m.method.getMethod().getAnnotation(Packet.class).priority().getPriority()));
                            } else {
                                methods.sort(Comparator.comparing(m ->
                                        m.method.getMethod().getAnnotation(dev.brighten.anticheat.check.api.Event.class)
                                                .priority().getPriority()));
                            }
                            checkMethods.put(parameter, methods);
                        });
            });
        }
    }
}
