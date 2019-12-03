package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import dev.brighten.api.check.CheckType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInfo {
    String name() default "yourmom";
    String description() default "No description provided.";
    boolean enabled() default true;
    boolean executable() default true;
    boolean developer() default false;
    int punishVL() default -1;
    CheckType checkType() default CheckType.SPEED;
    ProtocolVersion minVersion() default ProtocolVersion.V1_7;
    ProtocolVersion maxVersion() default ProtocolVersion.V1_14;

}
