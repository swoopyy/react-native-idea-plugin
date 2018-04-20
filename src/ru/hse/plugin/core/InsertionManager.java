package ru.hse.plugin.core;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import ru.hse.plugin.core.utils.ComponentInsertionEditorMouseAdapter;
import ru.hse.plugin.core.utils.SnippetInserted;

import java.util.ArrayList;
import java.util.List;

public class InsertionManager extends EditorManager {
    private static InsertionManager insertionManager;
    private Component component;

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component, SnippetInserted snippetInserted) {
        this.component = component;
        EditorMouseAdapter editorMouseAdapter = new ComponentInsertionEditorMouseAdapter(getProject(), getEditor().getSelectedTextEditor(), snippetInserted, component);
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
