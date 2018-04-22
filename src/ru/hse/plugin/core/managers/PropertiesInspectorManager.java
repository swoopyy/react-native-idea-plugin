package ru.hse.plugin.core.managers;

import ru.hse.plugin.core.callbacks.ComponentClicked;
import ru.hse.plugin.core.handlers.PropertiesInspectorEditorMouseAdapter;

public class PropertiesInspectorManager extends EditorManager {
    private static PropertiesInspectorManager propertiesInspectorManager;
    private ComponentClicked componentClicked;
    private PropertiesInspectorManager() {
        super();
    }

    public static void setHandler(ComponentClicked componentClicked) {
        PropertiesInspectorManager instance = getInstance();
        if (instance.componentClicked != componentClicked) {
            instance.componentClicked = componentClicked;
            instance.setEditorMouseAdapter(new PropertiesInspectorEditorMouseAdapter(componentClicked));
        }
    }

    public static void removeHandler() {
        getInstance().clear();
    }

    private static PropertiesInspectorManager getInstance() {
        if (propertiesInspectorManager == null) {
            propertiesInspectorManager = new PropertiesInspectorManager();
        }
        return propertiesInspectorManager;
    }

}
