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

package tschipp.carryon.config.forge;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tschipp.carryon.config.*;

import java.util.*;

public class ConfigLoaderImpl {

    public static final Map<ForgeConfigSpec, BuiltConfig> CONFIGS = new HashMap<>();

    public static void initialize() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ConfigLoaderImpl::onConfigLoad);
        bus.addListener(ConfigLoaderImpl::onConfigReload);

        ConfigLoaderImpl.CONFIGS.forEach((spec, config) -> {
            if(config.fileName.contains("client"))
                ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, spec, config.fileName+".toml");
            else
                ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec, config.fileName+".toml");
        });
    }

    public static void onConfigLoad(ModConfigEvent.Loading loading) {
        loadConfig(loading.getConfig().getSpec());
        ConfigLoader.onConfigLoaded();
    }

    public static void onConfigReload(ModConfigEvent.Reloading loading) {
        loadConfig(loading.getConfig().getSpec());
        ConfigLoader.onConfigLoaded();
    }

    private static void loadConfig(IConfigSpec<ForgeConfigSpec> spec) {
        BuiltConfig builtConfig = CONFIGS.get(spec.self());
        if (builtConfig == null) return;
        loadConfig(builtConfig, spec.self().getValues());
    }

    private static void loadConfig(BuiltCategory category, UnmodifiableConfig config) {
        config.valueMap().forEach((id, value) -> {
            if (value instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                category.getProperty(id).ifPresent(data -> {
                    if (configValue instanceof ForgeConfigSpec.BooleanValue booleanValue)
                        data.setBoolean(booleanValue.get());
                    if (configValue instanceof ForgeConfigSpec.IntValue intValue)
                        data.setInt(intValue.get());
                    if (configValue instanceof ForgeConfigSpec.DoubleValue doubleValue)
                        data.setDouble(doubleValue.get());
                    if(configValue.get() instanceof List<?> listVal)
                        data.setStringArray(listVal.toArray(new String[listVal.size()]));
                });
            } else if (value instanceof AbstractConfig subConfig) {
                category.getCategory(id).ifPresent(cat -> loadConfig(cat, subConfig));
            }
        });
    }

    public static void registerConfig(BuiltConfig config) {
        try {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            for (PropertyData property : config.properties) buildProperty(builder, property);
            for (BuiltCategory category : config.categories) buildCategory(builder, category);
            CONFIGS.put(builder.build(), config);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildCategory(ForgeConfigSpec.Builder builder, BuiltCategory category) throws IllegalAccessException {
        builder.push(category.category);
        if (category.categoryDesc != null) builder.comment(category.categoryDesc);
        for (PropertyData property : category.properties) buildProperty(builder, property);
        for (BuiltCategory builtCategory : category.categories) buildCategory(builder, builtCategory);
        builder.pop();
    }

    private static void buildProperty(ForgeConfigSpec.Builder builder, PropertyData data) throws IllegalAccessException {
        AnnotationData annotationData = data.data();
        builder.comment(annotationData.description());

        switch (annotationData.type()) {
            case BOOLEAN -> builder.define(data.getId(), data.getBoolean());
            case INT -> builder.defineInRange(data.getId(), data.getInt(), annotationData.min(), annotationData.max());
            case DOUBLE -> builder.defineInRange(data.getId(), data.getDouble(), annotationData.minD(), annotationData.maxD());
            case STRING_ARRAY -> builder.defineListAllowEmpty(List.of(data.getId()), () -> {
                try {
                    return Arrays.asList(data.getStringArray());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return new ArrayList<>();
            }, obj -> obj instanceof String);
            default -> throw new IllegalAccessException("Unknown property type.");
        }
    }
}
