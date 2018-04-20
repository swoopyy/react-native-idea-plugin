package ru.hse.plugin.core.managers;


import com.intellij.openapi.editor.event.EditorMouseAdapter;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.utils.ComponentInsertionEditorMouseAdapter;
import ru.hse.plugin.core.utils.SnippetInserted;

public class InsertionManager extends EditorManager {
    private static InsertionManager insertionManager;
    private Component component;

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component, SnippetInserted snippetInserted) {
        this.component = component;
        EditorMouseAdapter editorMouseAdapter = new ComponentInsertionEditorMouseAdapter(
                getProject(),
                getEditor(),
                snippetInserted,
                component
        );
        setEditorMouseAdapter(editorMouseAdapter);
    }

    private InsertionManager() {
       super();
    }

    public static InsertionManager getInstance() {
        if (insertionManager == null) {
            insertionManager = new InsertionManager();
        }
        return insertionManager;
    }

}
