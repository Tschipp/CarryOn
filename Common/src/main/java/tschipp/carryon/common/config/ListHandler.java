package tschipp.carryon.common.config;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import tschipp.carryon.Constants;
import tschipp.carryon.utils.StringHelper;

import java.util.*;
import java.util.stream.Collectors;

public class ListHandler {

    private static Set<String> FORBIDDEN_TILES = new HashSet<>();
    private static Set<String> FORBIDDEN_ENTITIES = new HashSet<>();
    private static Set<String> ALLOWED_ENTITIES = new HashSet<>();
    private static Set<String> ALLOWED_TILES = new HashSet<>();
    private static Set<String> FORBIDDEN_STACKING = new HashSet<>();
    private static Set<String> ALLOWED_STACKING = new HashSet<>();

    private static List<TagKey<Block>> FORBIDDEN_TILES_TAGS = new ArrayList<>();
    private static List<TagKey<EntityType<?>>> FORBIDDEN_ENTITIES_TAGS = new ArrayList<>();
    private static List<TagKey<EntityType<?>>> ALLOWED_ENTITIES_TAGS = new ArrayList<>();
    private static List<TagKey<Block>> ALLOWED_TILES_TAGS = new ArrayList<>();
    private static List<TagKey<EntityType<?>>> FORBIDDEN_STACKING_TAGS = new ArrayList<>();
    private static List<TagKey<EntityType<?>>> ALLOWED_STACKING_TAGS = new ArrayList<>();

    private static Set<Property<?>> PROPERTY_EXCEPTION_CLASSES = new HashSet<>();

    public static boolean isPermitted(Block block)
    {
        if(Constants.COMMON_CONFIG.settings.useWhitelistBlocks)
            return doCheck(block, ALLOWED_TILES, ALLOWED_TILES_TAGS);
        else
            return !doCheck(block, FORBIDDEN_TILES, FORBIDDEN_TILES_TAGS);
    }

    public static boolean isPermitted(Entity entity)
    {
        if(Constants.COMMON_CONFIG.settings.useWhitelistEntities)
            return doCheck(entity, ALLOWED_ENTITIES, ALLOWED_ENTITIES_TAGS);
        else
            return !doCheck(entity, FORBIDDEN_ENTITIES, FORBIDDEN_ENTITIES_TAGS);
    }

    public static boolean isStackingPermitted(Entity entity)
    {
        if(Constants.COMMON_CONFIG.settings.useWhitelistStacking)
            return doCheck(entity, ALLOWED_STACKING, ALLOWED_STACKING_TAGS);
        else
            return !doCheck(entity, FORBIDDEN_STACKING, FORBIDDEN_STACKING_TAGS);
    }

    public static boolean isPropertyException(Property<?> prop)
    {
        return PROPERTY_EXCEPTION_CLASSES.contains(prop);
    }

    private static boolean doCheck(Block block, Set<String> regular, List<TagKey<Block>> tags)
    {
        String name = BuiltInRegistries.BLOCK.getKey(block).toString();
        if(regular.contains(name))
            return true;
        for(TagKey<Block> tag : tags)
            if(block.defaultBlockState().is(tag))
                return true;
        return false;
    }

    private static boolean doCheck(Entity entity, Set<String> regular, List<TagKey<EntityType<?>>> tags)
    {
        String name = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if(regular.contains(name))
            return true;
        for(TagKey<EntityType<?>> tag : tags)
            if(entity.getType().is(tag))
                return true;
        return false;
    }

    public static void initConfigLists()
    {
        FORBIDDEN_ENTITIES.clear();
        FORBIDDEN_ENTITIES_TAGS.clear();
        FORBIDDEN_STACKING.clear();
        FORBIDDEN_STACKING_TAGS.clear();
        FORBIDDEN_TILES.clear();
        FORBIDDEN_TILES_TAGS.clear();
        ALLOWED_ENTITIES.clear();
        ALLOWED_ENTITIES_TAGS.clear();
        ALLOWED_STACKING.clear();
        ALLOWED_STACKING_TAGS.clear();
        ALLOWED_TILES.clear();
        ALLOWED_TILES_TAGS.clear();
        PROPERTY_EXCEPTION_CLASSES.clear();

        Map<ResourceLocation, TagKey<Block>> blocktags = BuiltInRegistries.BLOCK.getTagNames().collect(Collectors.toMap(t -> t.location(), t -> t));
        Map<ResourceLocation, TagKey<EntityType<?>>> entitytags = BuiltInRegistries.ENTITY_TYPE.getTagNames().collect(Collectors.toMap(t -> t.location(), t -> t));

        List<String> forbidden = new ArrayList<>(List.of(Constants.COMMON_CONFIG.blacklist.forbiddenTiles));
        forbidden.add("#carryon:block_blacklist");
        addWithWildcards(forbidden, FORBIDDEN_TILES, BuiltInRegistries.BLOCK, blocktags, FORBIDDEN_TILES_TAGS);

        List<String> forbiddenEntity = new ArrayList<>(List.of(Constants.COMMON_CONFIG.blacklist.forbiddenEntities));
        forbiddenEntity.add("#carryon:entity_blacklist");
        addWithWildcards(forbiddenEntity, FORBIDDEN_ENTITIES, BuiltInRegistries.ENTITY_TYPE, entitytags, FORBIDDEN_ENTITIES_TAGS);

        List<String> allowedEntities = new ArrayList<>(List.of(Constants.COMMON_CONFIG.whitelist.allowedEntities));
        allowedEntities.add("#carryon:entity_whitelist");
        addWithWildcards(allowedEntities, ALLOWED_ENTITIES, BuiltInRegistries.ENTITY_TYPE, entitytags, ALLOWED_ENTITIES_TAGS);

        List<String> allowedBlocks = new ArrayList<>(List.of(Constants.COMMON_CONFIG.whitelist.allowedBlocks));
        allowedBlocks.add("#carryon:block_whitelist");
        addWithWildcards(allowedBlocks, ALLOWED_TILES, BuiltInRegistries.BLOCK, blocktags, ALLOWED_TILES_TAGS);

        List<String> forbiddenStacking = new ArrayList<>(List.of(Constants.COMMON_CONFIG.blacklist.forbiddenStacking));
        forbiddenStacking.add("#carryon:stacking_blacklist");
        addWithWildcards(forbiddenStacking, FORBIDDEN_STACKING, BuiltInRegistries.ENTITY_TYPE, entitytags, FORBIDDEN_STACKING_TAGS);

        List<String> allowedStacking = new ArrayList<>(List.of(Constants.COMMON_CONFIG.whitelist.allowedStacking));
        allowedStacking.add("#carryon:stacking_whitelist");
        addWithWildcards(allowedStacking, ALLOWED_STACKING, BuiltInRegistries.ENTITY_TYPE, entitytags, ALLOWED_STACKING_TAGS);

        for(String propString : Constants.COMMON_CONFIG.settings.placementStateExceptions)
        {
            if(!propString.contains("[") || !propString.contains("]"))
                continue;
            String name = propString.substring(0, propString.indexOf("["));
            String props = propString.substring(propString.indexOf("[") + 1, propString.indexOf("]"));
            Block blk = BuiltInRegistries.BLOCK.get(new ResourceLocation(name));
            for(String propName : props.split(",")) {
                for (Property<?> prop : blk.defaultBlockState().getProperties()) {
                    if (prop.getName().equals(propName))
                        PROPERTY_EXCEPTION_CLASSES.add(prop);
                }
            }
        }
    }

    private static <T> void addTag(String tag, Map<ResourceLocation, TagKey<T>> tagMap, List<TagKey<T>> tags) {
        String sub = tag.substring(1);
        TagKey<T> t = tagMap.get(new ResourceLocation(sub));
        if (t != null)
            tags.add(t);
    }

    private static <T> void addWithWildcards(List<String> entries, Set<String> toAddTo, Registry<T> registry, Map<ResourceLocation, TagKey<T>> tags, List<TagKey<T>> toAddTags) {

        ResourceLocation[] keys = registry.keySet().toArray(new ResourceLocation[0]);
        for (int i = 0; i < entries.size(); i++)
        {
            String curr = entries.get(i);
            if (!curr.startsWith("#"))
            {
                if (curr.contains("*"))
                {
                    String[] filter = curr.replace("*", ",").split(",");

                    for (ResourceLocation key : keys)
                    {

                        if (containsAll(key.toString(), filter))
                        {
                            toAddTo.add(key.toString());
                        }
                    }
                }
                else
                    toAddTo.add(curr);
            }
            else
                addTag(curr, tags, toAddTags);
        }
    }

    public static boolean containsAll(String str, String... strings)
    {
        return StringHelper.matchesWildcards(str, strings);
    }

    public static void addForbiddenTiles(String toAdd)
    {
        FORBIDDEN_TILES.add(toAdd);
    }

    public static void addForbiddenEntities(String toAdd)
    {
        FORBIDDEN_ENTITIES.add(toAdd);
    }

    public static void addForbiddenStacking(String toAdd)
    {
        FORBIDDEN_STACKING.add(toAdd);
    }

    public static void addAllowedTiles(String toAdd)
    {
        ALLOWED_TILES.add(toAdd);
    }

    public static void addAllowedEntities(String toAdd)
    {
        ALLOWED_ENTITIES.add(toAdd);
    }

    public static void addAllowedStacking(String toAdd)
    {
        ALLOWED_ENTITIES.add(toAdd);
    }

}
