package de.crafttogether.craftbahn.localization;

public class PlaceholderResolver {
    private String name;
    private String value;

    private PlaceholderResolver(String key, String value) {
        this.name = key;
        this.value = value;
    }

    public static PlaceholderResolver resolver(String key, String value) {
        return new PlaceholderResolver(key, value);
    }

    public String parse(String text) {
        return text.replace("{" + this.name + "}", this.value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
