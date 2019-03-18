package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;

public @interface CheckInfo {
    String name() default "";
    String description() default "Blocks cheats";
    CheckType type() default CheckType.MOVEMENT;
    CancelType cancelType() default CancelType.MOTION;
    int maxVL() default 100;
    boolean enabled() default true;
    boolean executable() default true;
    boolean cancellable() default true;
    ProtocolVersion maxVersion() default ProtocolVersion.V1_13_2;

}
