package org.pioneer.api.configuration.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * CyConfiguration Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationProperty {

    String path() default "";

    String name();

    boolean isDefault() default true;

}
