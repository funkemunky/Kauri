package dev.brighten.anticheat.api.check;

import javax.annotation.processing.SupportedAnnotationTypes;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInfo {
    String name() default "yourmom";
    String description() default "No description provided.";
    boolean enabled() default true;
    boolean executable() default false;
    float maxVL() default 20f;
}
