package ru.hse.plugin.core;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Component {
    private String description;
    private Platform platform;
    private String url;
    private String iconPath;
    private Property[] properties;

    public Component() { }

    public Component(String name, String description, Platform platform, String url, String iconPath, Property[] properties) {
        this.description = description;
        this.platform = platform;
        this.url = url;
        this.iconPath = iconPath;
        this.properties = properties;
    }

    public static Component fromJsonNode(JsonNode jsonNode) {
        ArrayList<Property> properties = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.get("properties").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> elt = it.next();
            JsonNode property = elt.getValue();
            boolean isRequired = property.get("required").toString().equals("Yes");
            properties.add(
                    new Property(
                            elt.getKey(),
                            property.get("type").toString(),
                            isRequired
                    )
            );
        }

        Platform platform = Platform.BOTH;
        if (jsonNode.get("platform").toString().equals("android")) {
            platform = Platform.ANDROID;
        }
        if (jsonNode.get("platform").toString().equals("ios")) {
            platform = Platform.IOS;
        }

        return new Component(
                jsonNode.get("name").toString(),
                jsonNode.get("description").toString(),
                platform,
                jsonNode.get("url").toString(),
                "",
                properties.toArray(new Property[0])
        );
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
}
