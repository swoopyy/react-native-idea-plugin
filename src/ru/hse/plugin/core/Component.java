package ru.hse.plugin.core;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Component {
    private String name;
    private String description;
    private Platform platform;
    private String url;
    private String iconPath;
    private Property[] properties;
    private boolean isContainer;
    private boolean isDefault = false;
    private String importPath = "react-native";

    public Component() { }

    public Component(String name,
                     String description,
                     Platform platform,
                     String url,
                     String iconPath,
                     Property[] properties,
                     boolean isContainer
    ) {
        this.description = description;
        this.platform = platform;
        this.url = url;
        this.iconPath = iconPath;
        this.properties = properties;
        this.name = name;
        this.isContainer = isContainer;
    }

    public static Component fromJsonNode(JsonNode jsonNode) {
        ArrayList<Property> properties = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.get("properties").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> elt = it.next();
            JsonNode property = elt.getValue();
            boolean isRequired = property.get("required").textValue().equals("Yes");
            properties.add(
                    new Property(
                            elt.getKey(),
                            property.get("type").textValue(),
                            isRequired
                    )
            );
        }

        Platform platform = Platform.BOTH;
        if (jsonNode.get("platform").textValue().equals("android")) {
            platform = Platform.ANDROID;
        }
        if (jsonNode.get("platform").textValue().equals("ios")) {
            platform = Platform.IOS;
        }

        return new Component(
                jsonNode.get("name").textValue(),
                jsonNode.get("description").textValue(),
                platform,
                jsonNode.get("url").textValue(),
                "",
                properties.toArray(new Property[0]),
                jsonNode.get("isContainer").booleanValue()
        );
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(String importPath) {
        this.importPath = importPath;
    }

    public String getName() {
        return name;
    }

    public String getImportName() {
        if (name.contains(".")) {
            return name.split("\\.")[0];
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public void setContainer(boolean container) {
        isContainer = container;
    }

    public List<Property> getRequiredProperties() {
        List<Property> properties = new ArrayList<>();
        for (Property property: this.properties) {
            if (property.isRequired()) {
                properties.add(property);
            }
        }
        return properties;
    }

    public boolean meets(String searchTerm) {
        String lower = searchTerm.toLowerCase();
        return name.toLowerCase().indexOf(lower) != -1;
    }

    public String getSnippet() {
        String snippet = "\n<" + name + "\n";
        List<Property> properties = getRequiredProperties();
        for (Property property: properties) {
            snippet += "  " + property.getName() + "={}\n";
        }
        if (isContainer) {
            snippet += ">\n</" + name + ">\n";
        } else {
            snippet += "/>\n";
        }
        return snippet;
    }
    @Override
    public String toString() {
        return this.name;
    }
}
