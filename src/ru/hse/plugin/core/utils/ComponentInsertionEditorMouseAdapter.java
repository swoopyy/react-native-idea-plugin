package ru.hse.plugin.core.utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.project.Project;

public class ComponentInsertionEditorMouseAdapter extends EditorMouseAdapter {
    private String componentCode;
    private Editor editor;
    private Project project;
    private SnippetInserted snippetInserted;

    public ComponentInsertionEditorMouseAdapter(
            Project project,
            Editor editor,
            SnippetInserted snippetInserted,
            String componentCode
    ) {
        this.editor = editor;
        this.componentCode = componentCode;
        this.project = project;
        this.snippetInserted = snippetInserted;
    }

    @Override
    public void mouseClicked(EditorMouseEvent e) {
        int offset = editor.getCaretModel().getOffset();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().insertString(offset, componentCode);
            if (snippetInserted != null) {
                snippetInserted.perform();
            }
        });
    }
}
