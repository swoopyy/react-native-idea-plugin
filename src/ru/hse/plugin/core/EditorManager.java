package ru.hse.plugin.core;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import ru.hse.plugin.core.utils.ComponentInsertionEditorMouseAdapter;

import java.util.ArrayList;
import java.util.List;

public class EditorManager {
    private Project project;
    private Component component;
    private FileEditorManager fileEditorManager;
    private List<Editor> textEditors;
    EditorMouseAdapter editorMouseAdapter;

    public EditorManager() {
        project =  ProjectManager.getInstance().getOpenProjects()[0];
        fileEditorManager = FileEditorManager.getInstance(project);
        textEditors = new ArrayList<Editor>();
        addEditorListener();
    }

    protected void setEditorMouseAdapter(EditorMouseAdapter editorMouseAdapter) {
        update(editorMouseAdapter);
    }

    public void clear() {
        this.removeListeners();
    }

    protected Project getProject() {
        return project;
    }

    protected FileEditorManager getEditor() {
        return fileEditorManager;
    }


    private void removeListeners() {
        for (Editor editor : textEditors) {
            editor.removeEditorMouseListener(editorMouseAdapter);
        }
    }

    private void update(EditorMouseAdapter editorMouseAdapter) {
        removeListeners();
        Editor editor = fileEditorManager.getSelectedTextEditor();
        if (!textEditors.contains(editor)) {
            textEditors.add(editor);
        }
        if (editorMouseAdapter != null) {
            this.editorMouseAdapter = editorMouseAdapter;
        }
        editor.addEditorMouseListener(this.editorMouseAdapter);
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
                update(null);
            }
        });
    }
}
