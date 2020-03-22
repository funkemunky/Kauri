package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;

import java.util.List;

public class WrappedCheck {
    public String checkName;
    public WrappedMethod method;
    public Check check;
    public boolean isBoolean, oneParam, isPacket, isEvent;
    private boolean canRunWithVersion, didVersionCheck;
    public List<Class<?>> parameters;

    public WrappedCheck(Check check, WrappedMethod method) {
        this.check = check;
        this.checkName = check.getName();
        this.method = method;
        isBoolean = method.getMethod().getReturnType().equals(boolean.class);
        parameters = method.getParameters();
        oneParam = parameters.size() == 1;
        isPacket = method.getMethod().isAnnotationPresent(Packet.class);
        isEvent = method.getMethod().isAnnotationPresent(Event.class);
    }

    public boolean isCompatible() {
        if(didVersionCheck) return canRunWithVersion;

        if(!check.data.playerVersion.equals(ProtocolVersion.UNKNOWN)) {
            didVersionCheck = true;
            return this.canRunWithVersion = check.data.playerVersion.isOrBelow(check.maxVersion)
                    && check.data.playerVersion.isOrAbove(check.minVersion);
        }
        return true;
    }
}
