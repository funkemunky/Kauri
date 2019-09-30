package dev.brighten.anticheat.check.api;

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

}
