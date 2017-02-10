package com.dglab.cia.json.util;

import java.lang.annotation.*;

/**
 * Created by shoujo on 2/2/2017.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KeyValueTargets.class)
public @interface KeyValueTarget {
    String value() default "";
}
