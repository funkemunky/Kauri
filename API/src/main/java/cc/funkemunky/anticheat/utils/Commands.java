package cc.funkemunky.anticheat.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Commands {
    String[] commands() default "";
}
