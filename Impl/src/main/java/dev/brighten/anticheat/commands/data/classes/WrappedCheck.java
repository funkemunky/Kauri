package dev.brighten.anticheat.commands.data.classes;

import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import com.esotericsoftware.reflectasm.MethodAccess;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;

import java.util.List;

public class WrappedCheck {
    public String checkName;
    public MethodAccess access;
    public WrappedMethod method;
    public int methodIndex;
    public Check check;
    public boolean isBoolean, oneParam, isTick, isTimeStamp, isPacket, isEvent;
    private boolean canRunWithVersion, didVersionCheck;
    public List<Class<?>> parameters;

    public WrappedCheck(Check check, WrappedMethod method) {
        this.check = check;
        this.checkName = check.getName();
        isBoolean = method.getMethod().getReturnType().equals(boolean.class);
        parameters = method.getParameters();
        oneParam = parameters.size() == 1;
        this.access = MethodAccess.get(check.getClass());
        this.method = method;

        methodIndex = this.access.getIndex(method.getName(), method.getMethod().getParameterTypes());

        if(!oneParam) {
            isTick = method.getMethod().getParameterTypes()[1] == int.class;
            isTimeStamp = !isTick;
        }
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
