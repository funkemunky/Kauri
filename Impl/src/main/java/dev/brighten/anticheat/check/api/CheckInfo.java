package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInfo {
    String name() default "yourmom";
    String description() default "No description provided.";
    boolean enabled() default true;
    boolean executable() default false;
    boolean cancellable() default true;
    DevStage devStage() default DevStage.RELEASE;
    int punishVL() default 50;
    int vlToFlag() default -1;
    CheckType checkType() default CheckType.SPEED;
    ProtocolVersion minVersion() default ProtocolVersion.V1_7;
    ProtocolVersion maxVersion() default ProtocolVersion.v1_18_2;
}
