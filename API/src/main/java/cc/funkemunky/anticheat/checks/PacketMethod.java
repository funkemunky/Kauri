package cc.funkemunky.anticheat.checks;

public @interface PacketMethod {
    int priority() default 0;
}
