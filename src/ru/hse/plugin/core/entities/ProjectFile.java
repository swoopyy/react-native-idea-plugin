package ru.hse.plugin.core.entities;

import java.util.List;

public class ProjectFile {
    List<String> exportedComponents;
    List<String> defaultlyExportedComponents;
    String path;

    public ProjectFile(List<String> exportedComponents, List<String> defaultlyExportedComponents, String path) {
        this.exportedComponents = exportedComponents;
        this.defaultlyExportedComponents = defaultlyExportedComponents;
        this.path = path;
    }

    public List<String> getExportedComponents() {
        return exportedComponents;
    }

    public List<String> getDefaultlyExportedComponents() {
        return defaultlyExportedComponents;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        StringBuilder sbExp = new StringBuilder();
        StringBuilder sbDef = new StringBuilder();
        for (String component: exportedComponents) {
            sbExp.append(component);
        }
        for (String component: defaultlyExportedComponents) {
            sbDef.append(component);
        }
        return String.format(
                "Path: %s\n Exported components (%d): %s\n Default components (%d) %s",
                path,
                exportedComponents.size(),
                sbExp.toString(),
                defaultlyExportedComponents.size(),
                sbDef.toString()
        );
    }
}
