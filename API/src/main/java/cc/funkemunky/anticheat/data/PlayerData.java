package cc.funkemunky.anticheat.data;

import cc.funkemunky.anticheat.checks.Check;
import cc.funkemunky.anticheat.tinyprotocol.api.NMSObject;

import java.lang.reflect.Method;
import java.util.*;

public abstract class PlayerData {
    public UUID uuid;
    public Deque<Method> checkMethods = new LinkedList<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void callChecks(Object object) {
        checkMethods.stream().filter(method -> method.getName().equals("run") && method.getParameters()[0].getClass().getName().equals(object.getClass().getName())).forEach(method -> {
            method.
        });
    }
}
