package tschipp.carryon.config.annotations;

import tschipp.carryon.config.PropertyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {



    PropertyType type();
    String description();

    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;

    double minD() default Double.MIN_VALUE;
    double maxD() default Double.MAX_VALUE;
}
