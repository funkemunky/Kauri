package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.utils.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Packet {
    Priority priority() default Priority.NORMAL;
    boolean stopChecksOnFalse() default false;
}
