package ru.hse.plugin.core.managers;

import com.intellij.openapi.application.ApplicationAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import ru.hse.plugin.core.entities.Component;

import java.util.ArrayList;
import java.util.List;

public class  EditorManager {
    private Project project;
    private FileEditorManager fileEditorManager;
    private static Editor prevEditor;
    EditorMouseAdapter editorMouseAdapter;
    EditorMouseAdapter propertyInspectorMouseAdapter;

    public EditorManager() {
        project = ProjectManager.getInstance().getOpenProjects()[0];
        fileEditorManager = FileEditorManager.getInstance(project);
        addEditorListener();
    }

    protected void setEditorMouseAdapter(EditorMouseAdapter editorMouseAdapter) {
        update(editorMouseAdapter);
    }

    protected void setPropertyInspectorMouseAdapter(EditorMouseAdapter editorMouseAdapter) {
        propertyInspectorMouseAdapter = editorMouseAdapter;
        Editor editor = fileEditorManager.getSelectedTextEditor();
        editor.addEditorMouseListener(editorMouseAdapter);
    }

    public void clear() {
        this.removeListeners();
    }

    public Project getProject() {
        return project;
    }

    public Editor getEditor() {
        return fileEditorManager.getSelectedTextEditor();
    }


    private void removeListeners() {
        if (editorMouseAdapter != null && prevEditor != null) {
            prevEditor.removeEditorMouseListener(editorMouseAdapter);
        }
    }

    private void update(EditorMouseAdapter editorMouseAdapter) {
        removeListeners();
        Editor editor = fileEditorManager.getSelectedTextEditor();
        prevEditor = editor;
        if (editorMouseAdapter != null) {
            this.editorMouseAdapter = editorMouseAdapter;
            editor.addEditorMouseListener(this.editorMouseAdapter);
        } else {
            editor.removeEditorMouseListener(propertyInspectorMouseAdapter); // in case we already have this connected to editor
            editor.addEditorMouseListener(propertyInspectorMouseAdapter);
        }
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
