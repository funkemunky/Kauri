package cc.funkemunky.anticheat.checks;

import java.lang.reflect.Method;

//Credits: based this off of Luke's design: https://github.com/DeprecatedLuke/anticheat-base/blob/64b9accc21c652b96eb67e1b0e95f7cde0222545/src/main/java/com/ngxdev/anticheat/api/check/MethodWrapper.java
public class CallWrapper {
    public Check check;
    public Method method;
    public int priority;
}
