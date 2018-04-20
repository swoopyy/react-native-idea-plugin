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

public class InsertionManager {
    private static InsertionManager insertionManager;
    private Project project;
    private Component component;
    private FileEditorManager fileEditorManager;
    private List<Editor> textEditors;
    private EditorMouseAdapter editorMouseAdapter;
    private SnippetInserted snippetInserted;

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component, SnippetInserted snippetInserted) {
        this.component = component;
        this.snippetInserted = snippetInserted;
        update();
    }

    public void clear() {
        this.removeListeners();
    }

    private InsertionManager() {
        project =  ProjectManager.getInstance().getOpenProjects()[0];
        fileEditorManager = FileEditorManager.getInstance(project);
        textEditors = new ArrayList<Editor>();
        addEditorListener();
    }

    private void removeListeners() {
        for (Editor editor : textEditors) {
            editor.removeEditorMouseListener(editorMouseAdapter);
        }
    }

    private void update() {
        removeListeners();
        Editor editor = fileEditorManager.getSelectedTextEditor();
        if (!textEditors.contains(editor)) {
            textEditors.add(editor);
        }
        editorMouseAdapter = new ComponentInsertionEditorMouseAdapter(project, editor, snippetInserted, component);
        editor.addEditorMouseListener(editorMouseAdapter);
    }

    public static InsertionManager getInstance() {
        if (insertionManager == null) {
            insertionManager = new InsertionManager();
        }
        return insertionManager;
    }

    private void addEditorListener() {
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {

            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
               super.fileOpened(source, file);
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                super.fileClosed(source, file);
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                update();
            }
        });
    }
}
