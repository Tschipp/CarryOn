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

//Many Thanks to ThatGravyBoat for this template!

import tschipp.carryon.client.modeloverride.ModelOverrideHandler;
import tschipp.carryon.common.config.ListHandler;
import tschipp.carryon.common.pickupcondition.PickupConditionHandler;
import tschipp.carryon.config.annotations.Category;
import tschipp.carryon.config.annotations.Config;
import tschipp.carryon.config.annotations.Property;
import tschipp.carryon.platform.Services;

import java.lang.reflect.Field;

public class ConfigLoader {

    public static void registerConfig(Object object) {
        BuiltCategory category;
        try {
            category = buildCategory(null, object);
        } catch (Exception e) {
            e.printStackTrace();
            category = null;
        }
        if (category instanceof BuiltConfig config) {
            registerConfig(config);
        } else {
            throw new IllegalArgumentException("Config supplied does not have a @Config annotation");
        }
    }

    public static void registerConfig(BuiltConfig config) {
        Services.PLATFORM.registerConfig(config);
    }

    public static void onConfigLoaded() {
        ListHandler.initConfigLists();
        PickupConditionHandler.initPickupConditions();
        ModelOverrideHandler.initModelOverrides();
    }

    public static BuiltCategory buildCategory(String categoryDesc, Object object) throws IllegalAccessException {
        Class<?> configClass = object.getClass();
        BuiltCategory category;
        if (configClass.isAnnotationPresent(Config.class)) {
            category = new BuiltConfig(configClass.getAnnotation(Config.class).value());
        } else if (configClass.isAnnotationPresent(Category.class)) {
            category = new BuiltCategory(categoryDesc, configClass.getAnnotation(Category.class).value());
        } else {
            throw new IllegalStateException("Config does not contain any @Config annotation or @Category");
        }

        for (Field field : configClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class) && field.canAccess(object)) {
                PropertyType type = field.getAnnotation(Property.class).type();
                if (type.equals(PropertyType.CATEGORY)) {
                    category.categories.add(buildCategory(field.getAnnotation(Property.class).description(), field.get(object)));
                } else {
                    category.properties.add(new PropertyData(object, field, AnnotationData.getData(field)));
                }
            }
        }
        return category;
    }
}
