package tschipp.carryon.config;


import tschipp.carryon.config.annotations.Property;

import java.lang.reflect.Field;

public record AnnotationData(
        PropertyType type,
        String description,
        int min, int max,
        double minD, double maxD
) {

    public static AnnotationData getData(Field field) {
        Property annotation = field.getAnnotation(Property.class);
        return new AnnotationData(
                annotation.type(),
                annotation.description(),
                annotation.min(), annotation.max(),
                annotation.minD(), annotation.maxD()
        );
    }
}
