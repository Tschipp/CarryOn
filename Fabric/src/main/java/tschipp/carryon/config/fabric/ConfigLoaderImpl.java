package tschipp.carryon.config.fabric;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import tschipp.carryon.config.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoaderImpl {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    //Default JSON and config data.
    public static final Map<JsonObject, BuiltConfig> CONFIGS = new HashMap<>();

    public static void initialize() throws IOException {
        Path cfgPath = FabricLoader.getInstance().getConfigDir();

        for (Map.Entry<JsonObject, BuiltConfig> entry : CONFIGS.entrySet()) {
            File cfgFile = new File(cfgPath.toFile(), entry.getValue().fileName+".json");
            if (!cfgFile.exists()) {
                cfgPath.toFile().mkdirs();
                FileUtils.write(cfgFile, GSON.toJson(entry.getKey()), StandardCharsets.UTF_8);
            } else {
                JsonObject cfgJson = GSON.fromJson(FileUtils.readFileToString(cfgFile, StandardCharsets.UTF_8), JsonObject.class);
                FileUtils.write(cfgFile, GSON.toJson(loadConfig(entry.getValue(), cfgJson)), StandardCharsets.UTF_8);
            }
        }
    }

    private static JsonObject loadConfig(BuiltCategory category, JsonObject config) {
        config.entrySet().forEach((entry) -> {
            String id = entry.getKey();
            if (!id.startsWith("//")) {
                JsonElement value = entry.getValue();
                if (value instanceof JsonPrimitive configValue) {
                    category.getProperty(id).ifPresent(data -> {
                        if (configValue.isBoolean() && data.data().type().equals(PropertyType.BOOLEAN))
                            data.setBoolean(configValue.getAsBoolean());
                        if (configValue.isNumber() && data.data().type().equals(PropertyType.INT)) {
                            int configInt = configValue.getAsInt();
                            if (configInt > data.data().max() || configInt < data.data().min()) {
                                try {
                                    config.addProperty(id, data.getInt());
                                } catch (IllegalAccessException ignored) {
                                }
                            } else {
                                data.setInt(configInt);
                            }
                        }
                        if (configValue.isNumber() && data.data().type().equals(PropertyType.DOUBLE)) {
                            double configDouble = configValue.getAsDouble();
                            if (configDouble > data.data().maxD() || configDouble < data.data().minD()) {
                                try {
                                    config.addProperty(id, data.getDouble());
                                } catch (IllegalAccessException ignored) {
                                }
                            } else {
                                data.setDouble(configDouble);
                            }
                        }
                    });
                } else if (value instanceof JsonObject subConfig) {
                    category.getCategory(id).ifPresent(cat -> loadConfig(cat, subConfig));
                } else if (value instanceof JsonArray list) {
                    category.getProperty(id).ifPresent(data -> {
                        if(data.data().type() == PropertyType.STRING_ARRAY)
                        {
                            List<String> ls = new ArrayList<>();
                            for(JsonElement arrEle : list)
                            {
                                if(arrEle instanceof JsonPrimitive p && p.isString())
                                {
                                    ls.add(p.getAsString());
                                }
                            }
                            data.setStringArray(ls.toArray(new String[ls.size()]));
                        }
                    });
                }
            }
        });
        ConfigLoader.onConfigLoaded();
        return config;
    }

    public static void registerConfig(BuiltConfig config) {
        try {
            JsonObject configJson = new JsonObject();
            for (PropertyData property : config.properties) buildProperty(configJson, property);
            for (BuiltCategory category : config.categories) buildCategory(configJson, category);
            CONFIGS.put(configJson, config);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildCategory(JsonObject builder, BuiltCategory category) throws IllegalAccessException {
        JsonObject categoryJson = new JsonObject();
        if (category.categoryDesc != null) categoryJson.addProperty("//"+category.category, category.categoryDesc);
        for (PropertyData property : category.properties) buildProperty(categoryJson, property);
        for (BuiltCategory builtCategory : category.categories) buildCategory(categoryJson, builtCategory);
        builder.add(category.category, categoryJson);
    }

    private static void buildProperty(JsonObject builder, PropertyData data) throws IllegalAccessException {
        AnnotationData annotationData = data.data();
        builder.addProperty("//"+data.getId(), annotationData.description());

        switch (annotationData.type()) {
            case BOOLEAN -> builder.addProperty(data.getId(), data.getBoolean());
            case INT -> builder.addProperty(data.getId(), data.getInt());
            case DOUBLE -> builder.addProperty(data.getId(), data.getDouble());
            case STRING_ARRAY -> {
                JsonArray arr = new JsonArray();
                for(String s : data.getStringArray())
                    arr.add(s);
                builder.add(data.getId(), arr);
            }
            default -> throw new IllegalAccessException("Unknown property type.");
        }
    }
}
