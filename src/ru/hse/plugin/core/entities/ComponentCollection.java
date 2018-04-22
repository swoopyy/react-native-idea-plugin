package ru.hse.plugin.core.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;

import java.io.InputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComponentCollection {
    private static Component[] builtinComponents;

    static {
        try {
            loadBuiltInComponents();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void loadBuiltInComponents() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = ComponentCollection.class.getResourceAsStream("/components.json");
        JsonNode obj = mapper.readTree(is);
        ArrayList<Component> components = new ArrayList<>();
        for (final JsonNode objNode : obj) {
            components.add(Component.fromJsonNode(objNode));
        }
        builtinComponents = components.toArray(new Component[0]);
    }

    public static Component[] getBuiltinComponents() {
        return builtinComponents;
    }

    public static void setBuiltinComponents(Component[] builtinComponents) {
        ComponentCollection.builtinComponents = builtinComponents;
    }

    public static Component[] getCustomComponents() {
        return new Component[0];
    }

    public static Component[] getAllComponents() {
        Component[] customComponents = getCustomComponents();
        Component[] builtinComponents = getBuiltinComponents();
        List<Component> components = new ArrayList<>();
        for (Component component: customComponents) {
            components.add(component);
        }
        for (Component component: builtinComponents) {
            components.add(component);
        }
        return components.toArray(new Component[0]);
    }

    public static Component getComponent(String name) {
        Component[] components = getAllComponents();
        for(Component component: components) {
            if (component.getName().equals(name)) {
                return component;
            }
        }
        return null;
    }

}
