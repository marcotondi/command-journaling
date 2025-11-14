package dev.marcotondi.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to associate a Command implementation with its type name.
 * This is used by the CommandRegistry to dynamically discover and register commands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandType {
    /**
     * The unique type name of the command (e.g., "CreateUser").
     */
    String value();
}
