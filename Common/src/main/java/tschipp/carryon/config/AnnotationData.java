/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
