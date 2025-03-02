package project.game.engine.constant;

/**
 * Defines a game constant and its metadata.
 * This allows for more dynamic constant management.
 */
public class ConstantDefinition {
    private final String key;
    private final String category;
    private final Class<?> type;
    private final Object defaultValue;

    public ConstantDefinition(String key, String category, Class<?> type, Object defaultValue) {
        this.key = key;
        this.category = category;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getCategory() {
        return category;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}