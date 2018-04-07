package ru.hse.plugin.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

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


}
