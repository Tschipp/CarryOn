package tschipp.carryon.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuiltCategory {

    public final List<PropertyData> properties = new ArrayList<>();
    public final List<BuiltCategory> categories = new ArrayList<>();

    public final String categoryDesc;
    public final String category;

    public BuiltCategory(String categoryDesc, String category) {
        this.categoryDesc = categoryDesc;
        this.category = category;
    }

    public Optional<PropertyData> getProperty(String id) {
        for (PropertyData property : properties) {
            if (property.getId().equals(id)) {
                return Optional.of(property);
            }
        }
        return Optional.empty();
    }

    public Optional<BuiltCategory> getCategory(String id) {
        for (BuiltCategory category : categories) {
            if (category.category.equals(id)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
