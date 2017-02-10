package com.dglab.cia.json.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: kartemov
 * Date: 10.02.2017
 * Time: 22:48
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValueConsumer {
    Class<?> value();
    String[] ignored() default {};
}
